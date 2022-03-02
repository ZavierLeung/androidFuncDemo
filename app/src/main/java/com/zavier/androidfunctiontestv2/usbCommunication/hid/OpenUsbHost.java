package com.zavier.androidfunctiontestv2.usbCommunication.hid;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

public class OpenUsbHost {

    private static final String TAG = "OpenUsbHost";
    private UsbHidDevice mDevice;
    private Context mContext;
    private Handler mHandler;
    private static final int mSize = 1024;
    private static boolean mConnectFlag = false;
    private String mFileName, mFilePath;
    private static String mPacketType = "message";
    private int mPacketStatus = DataPacketUtils.SEND_STATUS_NULL;

    public OpenUsbHost(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
    }

    public void connectUsbHidDevice(int vid, int pid) {
        //UsbHidDevice device = UsbHidDevice.factory(this, 0x261A, 0x0C01);
        //mDevice = UsbHidDevice.factory(mContext, 0x2207, 0x0110);
        LogUtils.LogD(TAG,"vid: "+vid + " pid: "+ pid);

        mDevice = UsbHidDevice.factory(mContext, vid, pid);
        //mDevice = UsbHidDevice.factory(this, 0x261A, 0x0C01);
        if (mDevice == null) {
            Log.d("test","usb device failed");
            return;
        }
        mDevice.open(mContext, new OnUsbHidDeviceListener() {
            @Override
            public void onUsbHidDeviceConnected(UsbHidDevice device) {
                Log.d("onUsbHidDeviceConnected","usb hid connect");
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_SUCCESS,"");
                mConnectFlag = true;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        String sendBuffer;
                        byte[] sendBuffer = new byte[mSize];
                        int i = 0, size = 0, status = 0;
                        String messageType = null, fileName = null;
                        DataPacketUtils usbDataPacket = new DataPacketUtils();
                        while (mConnectFlag){
                            if(mDevice != null){
                                i = mDevice.read(sendBuffer, mSize,1000);
                                if(usbDataPacket.isPacketHeader(new String(sendBuffer))){
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
                                        writeDataFromAccessory(messageType,status,fileName);
                                        Arrays.fill(sendBuffer, (byte) 0);
                                    }
                                } else {
                                    if (i > 0) {
                                        LogUtils.LogD(TAG, "read i:" + i + "  " + "size: "+mSize);
                                        if(i < mSize){
                                            writeDataFromAccessory(sendBuffer, i,1);
                                        } else {
                                            writeDataFromAccessory(sendBuffer,mSize,0);
                                        }
                                        Arrays.fill(sendBuffer, (byte) 0);
                                    }
                                }
                            }
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void onUsbHidDeviceConnectFailed(UsbHidDevice device) {
                Log.d("DeviceConnectFailed","usb hid failed");
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_FAILED,"");
                mConnectFlag = false;
            }
        });
    }

    public void setConnectFlag(boolean b){
        mConnectFlag = b;
    }

    public boolean getConnectFlag(){
        return mConnectFlag;
    }

    public void closeHidHost(){
        if(mConnectFlag){
            mConnectFlag = false;
        }
        if(mDevice != null){
            try {
                mDevice.close();
                mDevice = null;
            } catch (NullPointerException e){
                LogUtils.LogE(TAG,"closeHidHost mDevice is null");
            }

        }
    }

    private void writeDataFromAccessory(String type, int status, String fileName){
        if("file".equals(type)){
            switch (status){
                case DataPacketUtils.SEND_STATUS_START:
                    if(mPacketStatus == DataPacketUtils.SEND_STATUS_NULL){
                        mPacketStatus = DataPacketUtils.SEND_STATUS_START;
                        FileUtils.deleteFile(LogUtils.FILE_DOWNLOAD_PATH, fileName);
//                        doMessage(MainActivity.HOST_RECEIVER_FILE_START, "");
                    }
                    break;
                case DataPacketUtils.SEND_STATUS_END:
                    String string = "receiver file path:" + LogUtils.FILE_DOWNLOAD_PATH + fileName +"  receiver " +
                            "success \n";
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_FILE_SUCCESS, string);
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

    private void writeDataFromAccessory(byte[] bytes, int size, int type){
        if("file".equals(mPacketType)){
            if(mPacketStatus == DataPacketUtils.SEND_STATUS_START){
                FileUtils.createFileWithByte(bytes, LogUtils.FILE_DOWNLOAD_PATH, mFileName,size,type);
            }
        } else if("message".equals(mPacketType)){
            LogUtils.LogD(TAG, "read size: "+size+ " sendBuffer:"+new String(bytes));

            if(mPacketStatus == DataPacketUtils.SEND_STATUS_START){
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_MESSAGE_SUCCESS,
                        "device: "+new String(bytes)+ "\n");
            }
        }
    }

    public void sendFileToHid(String filePath, int size){
        try {
            if(mDevice == null){
                return ;
            }
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buf = new byte[size];
            DataPacketUtils usbDataPacket = new DataPacketUtils();
            String packetHeaderString = null;
            String fileName = null;
            long fileLengh = file.length();
            long fileCount = 0;
            long startTime = 0, endTime = 0;
            for (int length; (length = fileInputStream.read(buf)) != -1;) {
                if(fileCount == 0){
                    packetHeaderString = usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_START, "file");
                    fileName = usbDataPacket.getFileNameFromPath(filePath);
                    mDevice.write(packetHeaderString.getBytes());
                    startTime = System.currentTimeMillis();
                }
                fileCount += length;
               // doLog(TAG," length: " + length + " size:"+size + " fileCount: "+fileCount);
                if(length < size){
                    mDevice.write(buf,0,length);
                    endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    DecimalFormat decimalFormat =new DecimalFormat("0.00");
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_FILE_SUCCESS,
                            "fileName: " +fileName+ "\n"+
                            "fileSize:" + decimalFormat.format((double)fileLengh/(1024*1024))  + "MB\n"
                                    + "sendtime: "+ decimalFormat.format((double)time / 1000) + "s\n"
                                    + "saverage speed: "+ decimalFormat.format(((double)fileLengh*1000 / (time * 1024)))+ "KB/s\n"
                                    + "send success \n");
                } else if (length == size){
                    mDevice.write(buf);
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SENDING_FILE, " send state: "+ (double)(fileCount*100/fileLengh) + "% \n");
                }
            }
            mDevice.write(usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_END, "file").getBytes());
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToHid(String messageContent){
        String s = "message#test#"+ DataPacketUtils.SEND_STATUS_START+"#";
        mDevice.write(DataPacketUtils.getPacketHeader(s).getBytes(),DataPacketUtils.getPacketHeader(s).length());
        LogUtils.LogD(TAG, "sendMessageToHid length: "+messageContent.getBytes().length+ " " +
                "messageContent" + ":"+new String(messageContent.getBytes()));
        mDevice.write(messageContent.getBytes(), messageContent.length());

        s = "message#test#"+DataPacketUtils.SEND_STATUS_END+"#";
        mDevice.write(DataPacketUtils.getPacketHeader(s).getBytes(),DataPacketUtils.getPacketHeader(s).length());

        MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_MESSAGE_SUCCESS, "write: "+messageContent+"\n");
    }
}
