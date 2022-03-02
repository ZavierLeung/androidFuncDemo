package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.DataPacketUtils;
import com.zavier.androidfunctiontestv2.customUtils.FileUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class HostDevice {

    private static final String TAG = "HostDevice";
    private ExecutorService mThreadPool;
    private UsbManager mUsbManager;
    private Context mContext;
    private Handler mHandler;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private int mSize = 1024 *10; //bulkTransfer不能超过16K
    private byte[] mBytes = new byte[mSize];
    private String mFileName, mFilePath;
    private int mPacketStatus = DataPacketUtils.SEND_STATUS_NULL;
    private String mPacketType = "message";
    private boolean mUsbConnect = true;

    public HostDevice(Context context, Handler handler, ExecutorService executorService,
                      UsbManager usbManager){
        this.mContext = context;
        this.mHandler = handler;
        this.mThreadPool = executorService;
        this.mUsbManager = usbManager;
        checkHostAccessory();
    }

    public void checkHostAccessory(){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(MessageUtils.USB_ACTION), 0);

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory usbAccessory = (accessories == null ? null : accessories[0]);
        if (usbAccessory != null) {
            if (mUsbManager.hasPermission(usbAccessory)) {
                initHostAccessory(usbAccessory);
            } else {
                mUsbManager.requestPermission(usbAccessory, pendingIntent);
            }
        }
    }

    /**
     * 打开Accessory模式
     *
     * @param usbAccessory
     */
    public void initHostAccessory(UsbAccessory usbAccessory) {
        LogUtils.LogD(TAG,"enter initHostAccessory");

        mParcelFileDescriptor = mUsbManager.openAccessory(usbAccessory);
        if (mParcelFileDescriptor != null) {
            mHandler.sendEmptyMessage(MessageUtils.MSG_USB_CONNECTED_SUCCESS);
            FileDescriptor fileDescriptor = mParcelFileDescriptor.getFileDescriptor();
            //readFileAccessory(fileDescriptor);
            mFileInputStream = new FileInputStream(fileDescriptor);
            mFileOutputStream = new FileOutputStream(fileDescriptor);

            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    int i = 0, size = 0, status = 0;
                    String messageType = null, fileName = null;
                    DataPacketUtils usbDataPacket = new DataPacketUtils();
                    while (i >= 0 && mUsbConnect) {
                        try {
                            i  = mFileInputStream.read(mBytes);
                            LogUtils.LogD(TAG,"openAccessory i: "+i + " size: "+mBytes.length);
                            if(usbDataPacket.isPacketHeader(new String(mBytes))){
                                status = usbDataPacket.getFileStatus();
                                messageType = usbDataPacket.getMessageType();
                                fileName = usbDataPacket.getFileName();
                                size = usbDataPacket.getFileContentLength();
                                LogUtils.LogD(TAG,messageType);
                                LogUtils.LogD(TAG,fileName);
                                LogUtils.LogD(TAG,String.valueOf(size));
                                LogUtils.LogD(TAG,String.valueOf(status));
                                if (i > 0) {
                                    mPacketType = messageType;
                                    mFileName = fileName;
                                    writeDataFromHost(messageType,status,fileName);
                                    Arrays.fill(mBytes, (byte) 0);
                                }
                            } else {
                                if (i > 0) {
                                    if(i < mSize){
                                        writeDataFromHost(mBytes,i, MessageUtils.RECEIVED_END);
                                    } else {
                                        writeDataFromHost(mBytes,mBytes.length, MessageUtils.RECEIVING);
                                    }
                                    Arrays.fill(mBytes, (byte) 0);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void writeDataFromHost(String type, int status, String fileName){
        if("file".equals(type)){
            switch (status){
                case DataPacketUtils.SEND_STATUS_START:
                    if(mPacketStatus == DataPacketUtils.SEND_STATUS_NULL){
                        mPacketStatus = DataPacketUtils.SEND_STATUS_START;
                        FileUtils.deleteFile(LogUtils.FILE_DOWNLOAD_PATH, fileName);
                        MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_FILE_SUCCESS, "");
                    }
                    break;
                case DataPacketUtils.SEND_STATUS_END:
                    String string = "file path:" + LogUtils.FILE_DOWNLOAD_PATH + fileName +"  " +
                            "receiver " + "success \n";
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_FILE_SUCCESS, string);
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

    private void writeDataFromHost(byte[] bytes, int size, int type){
        if("file".equals(mPacketType)){
            if(mPacketStatus == DataPacketUtils.SEND_STATUS_START){
                FileUtils.createFileWithByte(bytes, LogUtils.FILE_DOWNLOAD_PATH, mFileName,size,type);
            }
        } else if("message".equals(mPacketType)){
            if(mPacketStatus == DataPacketUtils.SEND_STATUS_START){
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_MESSAGE_SUCCESS, "receiver: "+new String(bytes)+"\n");
            }
        }
    }

    public void sendMessageToSlave(String messageContent){
        try {
            if(mFileOutputStream != null){
                String s = "message#test#"+DataPacketUtils.SEND_STATUS_START+"#";
                mFileOutputStream.write(DataPacketUtils.getPacketHeader(s).getBytes());
                mFileOutputStream.write(messageContent.getBytes());
                mHandler.sendEmptyMessage(MessageUtils.MSG_USB_SEND_MESSAGE_SUCCESS);
                s = "message#test#"+DataPacketUtils.SEND_STATUS_END+"#";
                mFileOutputStream.write(DataPacketUtils.getPacketHeader(s).getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileToSlave(String filePath, int size){
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            DataPacketUtils usbDataPacket = new DataPacketUtils();
            String packetHeaderString = null;
            String fileName = null;
            byte[] buf = new byte[size];
            long fileLengh = file.length();
            long fileCount = 0;
            long startTime = 0, endTime = 0;
            for (int length; (length = fileInputStream.read(buf)) != -1;) {
                if(fileCount == 0){
                    packetHeaderString = usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_START, "file");
                    fileName = usbDataPacket.getFileNameFromPath(filePath);
                    mFileOutputStream.write(packetHeaderString.getBytes());
                    startTime = System.currentTimeMillis();
                    LogUtils.LogD(TAG," start fileLengh: "+ fileLengh + " fileSize: "+ size + " " + "length:"+length);
                }
                fileCount += length;
                if(length < size){
                    LogUtils.LogD(TAG," end fileLengh: "+ fileLengh + " fileSize: "+ size + " " + "length:"+length);
                    mFileOutputStream.write(buf, 0, length);
                    endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    DecimalFormat decimalFormat =new DecimalFormat("0.00");
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_FILE_SUCCESS,
                            "fileName" +
                            ": " +fileName+ "\n"
                            +"fileSize:" + decimalFormat.format((double)fileLengh/(1024*1024))  + "MB\n"
                            + "sendtime: "+ decimalFormat.format((double)time / 1000) + "s\n"
                            + "saverage speed: "+ decimalFormat.format(((double)fileLengh*1000 / (time * 1024)))+ "KB/s\n"
                            + "send success \n");
                } else if (length == size){
                    mFileOutputStream.write(buf);
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_FILE_SUCCESS,  " send state: "+ (double)(fileCount*100/fileLengh) + "% \n");
                }
            }
            mFileOutputStream.write(usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_END, "file").getBytes());
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHostUsbConnect(boolean b){
        mUsbConnect = b;
    }

    public void closeHostDevice(){
        MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_FAILED,
                mContext.getResources().getString(R.string.usb_communication_usb_disconnect));
        mUsbConnect = false;
        try {
            if (mParcelFileDescriptor != null) {
                LogUtils.LogD(TAG, "enter mParcelFileDescriptor");
                mParcelFileDescriptor.close();
            }
            if (mFileInputStream != null) {
                LogUtils.LogD(TAG, "enter mFileInputStream");
                mFileInputStream.close();
            }
            if (mFileOutputStream != null) {
                LogUtils.LogD(TAG, "enter mFileOutputStream");
                mFileOutputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
