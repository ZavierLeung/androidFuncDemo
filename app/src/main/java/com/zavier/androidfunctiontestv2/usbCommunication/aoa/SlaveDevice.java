package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.SystemClock;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.DataPacketUtils;
import com.zavier.androidfunctiontestv2.customUtils.FileUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class SlaveDevice {

    private static final String TAG = "SlaveDevice";
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbEndpoint mUsbEndpointOut;
    private UsbEndpoint mUsbEndpointIn;
    private ExecutorService mThreadPool;
    private UsbManager mUsbManager;
    private boolean isReceiverMessage = true;
    private int mSize = 1024*10;    //bulkTransfer不能超过16K
    private byte[] mBytes = new byte[mSize];
    private boolean isDetached = false;
    private boolean mHostToggle = true;
    private UsbInterface mHostUsbInterface;
    private Context mContext;
    private Handler mHandler;
    private int mPacketStatus = DataPacketUtils.SEND_STATUS_NULL;
    private String mPacketType = "message";
    private String mFileName;

    public SlaveDevice(Context context, Handler handler, ExecutorService executorService,
                       UsbManager usbManager){
        this.mContext = context;
        this.mHandler = handler;
        this.mThreadPool = executorService;
        this.mUsbManager = usbManager;
    }

    public boolean getUsbDetached(){
        return isDetached;
    }


    public void checkSlaveAccessory(){
        //设备广播
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(MessageUtils.USB_ACTION), 0);

        //列举设备(手机)
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        if (deviceList != null) {
            for (UsbDevice usbDevice : deviceList.values()) {
                int productId = usbDevice.getProductId();
                if (productId != 377 && productId != 7205) {
                    LogUtils.LogD(TAG, usbDevice.getDeviceName() + " , " +usbDevice.getManufacturerName());
                    if (mUsbManager.hasPermission(usbDevice)) {
                        initSlaveAccessory(usbDevice);
                    } else {
                        mUsbManager.requestPermission(usbDevice, pendingIntent);
                    }
                }
            }
        }
    }
    /**
     * 发送命令 , 让手机进入Accessory模式
     *
     * @param usbDevice
     */
    public void initSlaveAccessory(UsbDevice usbDevice) {
        UsbDeviceConnection usbDeviceConnection = mUsbManager.openDevice(usbDevice);
        if (usbDeviceConnection == null) {
            LogUtils.LogD(TAG, "initSlaveAccessory usb disconnect");
            MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_ERR,
                    mContext.getResources().getString(R.string.usb_communication_connect_usb));
            return;
        }

        //根据AOA协议打开Accessory模式
        initStringControlTransfer(usbDeviceConnection, 0, "Google, Inc."); // MANUFACTURER
        initStringControlTransfer(usbDeviceConnection, 1, "UsbCommunication"); // MODEL
        initStringControlTransfer(usbDeviceConnection, 2, "Accessory Usb"); // DESCRIPTION
        initStringControlTransfer(usbDeviceConnection, 3, "1.0"); // VERSION
        initStringControlTransfer(usbDeviceConnection, 4, "http://www.android.com"); // URI
        initStringControlTransfer(usbDeviceConnection, 5, "0123456789"); // SERIAL
        //0x40相当于USB_TYPE_VENDOR，53是请求连接的设备在accessory模式启动，52是向设备发送标识字符串信息
        usbDeviceConnection.controlTransfer(0x40, 53, 0, 0, new byte[]{}, 0, 100);
        usbDeviceConnection.close();
        LogUtils.LogD(TAG, mContext.getResources().getString(R.string.usb_aoa_initAccessory_success));
        mHostToggle = true;
        isReceiverMessage = true;
        isDetached = false;
        initDevice();
    }

    private void initStringControlTransfer(UsbDeviceConnection deviceConnection, int index, String string) {
        deviceConnection.controlTransfer(0x40, 52, 0, index, string.getBytes(), string.length(), 100);
    }

    public int doBulkTransfer(String messageContent) {
        return mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, messageContent.getBytes(), messageContent.getBytes().length, 3000);
    }

    public int doBulkByteTransfer(byte[] buf) {
        return mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, buf, buf.length, 3000);
    }

    public int doBulkByteTransfer(byte[] buf, int length) {
        return mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, buf, length, 3000);
    }

    /**
     * 接受消息线程 , 此线程在设备(手机)初始化完成后 , 就一直循环接受消息
     */
    public void loopReceiverMessage() {
        LogUtils.LogD(TAG,"loopReceiverMessage enter");
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                int i = 0, size = 0, status = 0;
                String messageType = null, fileName = null;
                DataPacketUtils usbDataPacket = new DataPacketUtils();
                while (isReceiverMessage) {
                    /**
                     * 循环接受数据的地方 , 只接受byte数据类型的数据
                     */
                    if (mUsbDeviceConnection != null && mUsbEndpointIn != null) {
                        i = mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, mBytes, mBytes.length, 3000);
                        LogUtils.LogD(TAG,"zavier i: "+i + " size: "+mBytes.length);
                        if(usbDataPacket.isPacketHeader(new String(mBytes))){
                            status = usbDataPacket.getFileStatus();
                            messageType = usbDataPacket.getMessageType();
                            fileName = usbDataPacket.getFileName();
                            size = usbDataPacket.getFileContentLength();
                            LogUtils.LogD(TAG,messageType);
                            LogUtils.LogD(TAG,fileName);
                            LogUtils.LogD(TAG, String.valueOf(size));
                            LogUtils.LogD(TAG, String.valueOf(status));

                            if (i > 0) {
                                mPacketType = messageType;
                                mFileName = fileName;
                                writeDataFromSlave(messageType,status,fileName);
                                Arrays.fill(mBytes, (byte) 0);
                            }
                        } else {
                            if (i > 0) {
                                if(i < mSize){
                                    writeDataFromSlave(mBytes,i,1);
                                } else {
                                    writeDataFromSlave(mBytes,mBytes.length,0);
                                }
                                Arrays.fill(mBytes, (byte) 0);
                            }
                        }
                    }
                }
            }
        });
    }

    private void writeDataFromSlave(String type, int status, String fileName){
        if("file".equals(type)){
            switch (status){
                case DataPacketUtils.SEND_STATUS_START:
                    if(mPacketStatus == DataPacketUtils.SEND_STATUS_NULL){
                        mPacketStatus = DataPacketUtils.SEND_STATUS_START;
                        FileUtils.deleteFile(LogUtils.FILE_DOWNLOAD_PATH, fileName);
                        MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_FILE_START, "");
                    }
                    break;
                case DataPacketUtils.SEND_STATUS_END:
                    String string = "receiver file path:" + LogUtils.FILE_DOWNLOAD_PATH + fileName +"  " +
                            "receiver success \n";
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_FILE_SUCCESS,
                            string);
                case DataPacketUtils.SEND_STATUS_NULL:
                    mPacketStatus = DataPacketUtils.SEND_STATUS_NULL;
                    break;
            }
        } else if("message".equals(type)){
            switch (status) {
                case DataPacketUtils.SEND_STATUS_START:
                case DataPacketUtils.SEND_STATUS_MIDDLE:
                    mPacketStatus = DataPacketUtils.SEND_STATUS_START;
                    break;
                case DataPacketUtils.SEND_STATUS_END:
                case DataPacketUtils.SEND_STATUS_NULL:
                    mPacketStatus = DataPacketUtils.SEND_STATUS_NULL;
                    break;
            }
        }
    }

    private void writeDataFromSlave(byte[] bytes, int size, int type){
        if("file".equals(mPacketType)){
            if(mPacketStatus == DataPacketUtils.SEND_STATUS_START){
                FileUtils.createFileWithByte(bytes, LogUtils.FILE_DOWNLOAD_PATH, mFileName,size,type);
            }
        } else if("message".equals(mPacketType)){
            if(mPacketStatus == DataPacketUtils.SEND_STATUS_START){
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_MESSAGE_SUCCESS,
                        "receiver: "+new String(bytes)+"\n");
            }
        }
    }

    /**
     * 初始化设备(手机) , 当手机进入Accessory模式后 , 手机的PID会变为Google定义的2个常量值其中的一个 ,
     */
    private void initDevice() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (mHostToggle) {
                    SystemClock.sleep(1000);
                    HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                    Collection<UsbDevice> values = deviceList.values();
                    if (!values.isEmpty()) {
                        for (UsbDevice usbDevice : values) {
                            int productId = usbDevice.getProductId();
                            if (productId == 0x2D00 || productId == 0x2D01) {
                                if (mUsbManager.hasPermission(usbDevice)) {
                                    mUsbDeviceConnection = mUsbManager.openDevice(usbDevice);
                                    if (mUsbDeviceConnection != null) {
                                        mHostUsbInterface = usbDevice.getInterface(0);
                                        int endpointCount = mHostUsbInterface.getEndpointCount();
                                        for (int i = 0; i < endpointCount; i++) {
                                            UsbEndpoint usbEndpoint = mHostUsbInterface.getEndpoint(i);
                                            if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                                                    mUsbEndpointOut = usbEndpoint;
                                                } else if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                                                    mUsbEndpointIn = usbEndpoint;
                                                }
                                            }
                                        }
                                        if (mUsbEndpointOut != null && mUsbEndpointIn != null && !isDetached) {
                                            LogUtils.LogD(TAG,"connected success");
                                            mHandler.sendEmptyMessage(MessageUtils.MSG_USB_CONNECTED_SUCCESS);
                                            mHostToggle = false;
                                            isDetached = true;
                                            loopReceiverMessage();
                                        }
                                    }
                                } else {
                                    mUsbManager.requestPermission(usbDevice,
                                            PendingIntent.getBroadcast(mContext, 0, new Intent(MessageUtils.USB_ACTION), 0));
                                }
                            }
                        }
                    } else {
                       // finish();
                        LogUtils.LogD(TAG,"initDevice failed");
                    }
                }
            }
        });
    }

    public void sendMessageToHost(String messageContent){
        //发开始消息
        LogUtils.LogD(TAG,"sendMessageToHost ："+messageContent);

        String s = "message#test#"+DataPacketUtils.SEND_STATUS_START+"#";
        doBulkTransfer(DataPacketUtils.getPacketHeader(s));
        int i = doBulkTransfer(messageContent);
//          sendMessage(HOST_SEND_FILE_SUCCESS,"mSendType: "+ mSendType + "packetHeader: "+s+"\n");
        if (i > 0) {//大于0表示发送成功
            mHandler.sendEmptyMessage(MessageUtils.MSG_USB_SEND_MESSAGE_SUCCESS);
            //发结束消息
            s = "message#test#"+DataPacketUtils.SEND_STATUS_END+"#";
            doBulkTransfer(DataPacketUtils.getPacketHeader(s));
        }
    }

    public void sendFileToHost(String filePath, int size){
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            DataPacketUtils usbDataPacket = new DataPacketUtils();
            String packetHeaderString = null;
            byte[] buf = new byte[size];
            long fileLengh = file.length();
            long fileCount = 0;
            long startTime = 0, endTime = 0;
            int i = 0;
            String fileName = null;
            for (int length; (length = fileInputStream.read(buf)) != -1;) {
                if(fileCount == 0){
                    packetHeaderString = usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_START, "file");
                    fileName = usbDataPacket.getFileNameFromPath(filePath);
                    doBulkTransfer(packetHeaderString);
                    startTime = System.currentTimeMillis();
                }
                fileCount += length;
                if(length < size){
                    i = doBulkByteTransfer(buf,length);
                } else if (length == size){
                    i = doBulkByteTransfer(buf);
                }

//                String string = "packet: " + packetHeaderString +" size: "+ buf.length + " " +
//                        "length: "+ length + "fileCount: "+fileCount+ " fileLengh: "+fileLengh+" doBulkByteTransfer: "+i+" send: "+ (double) (fileCount*100/fileLengh) + "% \n";
//                mFileUnits.sendMessage(UsbStartActivity.HOST_SEND_FILE_SUCCESS,string);
                if (i > 0 && length < size) {//大于0表示发送成功
                    endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    DecimalFormat decimalFormat =new DecimalFormat("0.00");
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_FILE_SUCCESS, "fileName: " +fileName+ "\n"
                            +"fileSize:" + decimalFormat.format((double)fileLengh/(1024*1024))  + "MB\n"
                            + "sendtime: "+ decimalFormat.format((double)time / 1000) + "s\n"
                            + "saverage speed: "+ decimalFormat.format(((double)fileLengh*1000 / (time * 1024)))+ "KB/s\n"
                            + "send success \n");
                } else if(i > 0 && length == size){
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_FILE_SUCCESS,  " send state: "+ (double)(fileCount*100/fileLengh) + "% \n");
                }
            }
            doBulkTransfer(usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_END, "file"));
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSlaveDevice(){
        MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_FAILED,
                mContext.getResources().getString(R.string.usb_communication_usb_disconnect));
        isReceiverMessage = false;
        if (mUsbDeviceConnection != null) {
            mUsbDeviceConnection.releaseInterface(mHostUsbInterface);
            mUsbDeviceConnection.close();
            mUsbDeviceConnection = null;
        }
        mUsbEndpointIn = null;
        mUsbEndpointOut = null;
        mHostToggle = false;
    }
}
