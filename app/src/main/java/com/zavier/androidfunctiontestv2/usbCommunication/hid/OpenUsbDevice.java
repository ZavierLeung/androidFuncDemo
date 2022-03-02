package com.zavier.androidfunctiontestv2.usbCommunication.hid;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.zavier.androidfunctiontestv2.customUtils.DataPacketUtils;
import com.zavier.androidfunctiontestv2.customUtils.FileUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;

public class OpenUsbDevice {
    private static final String TAG = "OpenUsbDevice";
    private Context mContext;
    private Handler mHandler;
    private OpenUsbHidPort mOpenUsbHidPort;
    private OpenUsbHost mOpenUsbHost;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private boolean mHidStatus = false;
    private int mHidSize = 1024;
    private int mPacketStatus = DataPacketUtils.SEND_STATUS_NULL;
    private String mFileName , mFilePath;
    private static String mPacketType = "message";


    public OpenUsbDevice(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
    }

    public void connectUsbHidHost(){
        if(initHid()){
            loopReceiverMessage();
        }
    }

    private boolean initHid(){
        try {
            String path = "/dev/hidg0" ;
            /* Open the serial port */
            mOpenUsbHidPort = new OpenUsbHidPort(new File(path), 0, 0);
            mOutputStream = mOpenUsbHidPort.getOutputStream();
            mInputStream = mOpenUsbHidPort.getInputStream();
            mHidStatus = mOpenUsbHidPort.getHidPortFlag();
            if(mHidStatus && mInputStream!=null && mOutputStream!= null){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 接受消息线程 , 一直循环接受消息
     */
    public void loopReceiverMessage() {
        MessageUtils.sendEmptyMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_SUCCESS);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] sendBuffer = new byte[mHidSize];
                    int i = 0, size = 0, status = 0;
                    String messageType = null, fileName = null;
                    DataPacketUtils usbDataPacket = new DataPacketUtils();
                    while (mHidStatus){
                        i = mInputStream.read(sendBuffer,0,mHidSize);
                        LogUtils.LogD(TAG,"openHidDevice i: "+i + " size: "+sendBuffer.length);
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
                                writeDataFromHost(messageType,status,fileName);
                                Arrays.fill(sendBuffer, (byte) 0);
                            }
                        } else {
                            if (i > 0) {
                                LogUtils.LogD(TAG, "read i:" + i + "  size: "+mHidSize);
                                if(i < mHidSize){
                                    writeDataFromHost(sendBuffer,i,1);
                                } else {
                                    writeDataFromHost(sendBuffer,sendBuffer.length,0);
                                }
                                Arrays.fill(sendBuffer, (byte) 0);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    public void sendFileToHost(String filePath, int size){
        try {
            if(mOutputStream == null){
                return ;
            }
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buf = new byte[size];
            DataPacketUtils usbDataPacket = new DataPacketUtils();
            long fileLengh = file.length();
            String fileName = null;
            String packetHeaderString = null;
            long fileCount = 0;
            long startTime = 0, endTime = 0;
            for (int length; (length = fileInputStream.read(buf)) != -1;) {
                if(fileCount == 0){
                    packetHeaderString = usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_START, "file");
                    fileName = usbDataPacket.getFileNameFromPath(filePath);
                    mOutputStream.write(packetHeaderString.getBytes());
                    startTime = System.currentTimeMillis();
                    LogUtils.LogD(TAG," start fileLengh: "+ fileLengh + " fileSize: "+ size + " " +
                            "length:"+length);
                }
                fileCount += length;
//                doLog(TAG," length: " + length + " size:"+size + " fileCount: "+fileCount);
                if(length < size){
                    mOutputStream.write(buf,0,length);
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
                    mOutputStream.write(buf,0,size);
                    MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SENDING_FILE, " send state: "+ (double)(fileCount*100/fileLengh) + "% \n");
                }
            }
            mOutputStream.write(usbDataPacket.getPacketHeader(filePath,DataPacketUtils.SEND_STATUS_END, "file").getBytes());
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToHost(String messageContent){
        try {
            if(mOutputStream == null){
                return ;
            }
            String s = "message#test#"+DataPacketUtils.SEND_STATUS_START+"#";
            mOutputStream.write(DataPacketUtils.getPacketHeader(s).getBytes());
            mOutputStream.flush();
            mOutputStream.write(messageContent.getBytes(),0,messageContent.length());
            mOutputStream.flush();
            s = "message#test#"+DataPacketUtils.SEND_STATUS_END+"#";
            mOutputStream.write(DataPacketUtils.getPacketHeader(s).getBytes());
            LogUtils.LogD(TAG, "sendBuffer :" + messageContent);
            MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_SEND_MESSAGE_SUCCESS, "Hid: " + messageContent + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDataFromHost(String type, int status, String fileName){
        if("file".equals(type)){
            switch (status){
                case DataPacketUtils.SEND_STATUS_START:
                    if(mPacketStatus == DataPacketUtils.SEND_STATUS_NULL){
                        mPacketStatus = DataPacketUtils.SEND_STATUS_START;
                        FileUtils.deleteFile(LogUtils.FILE_DOWNLOAD_PATH, fileName);
//                        MessageUtils.sendEmptyMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_FILE_SUCCESS);
                    }
                    break;
                case DataPacketUtils.SEND_STATUS_END:
                    String string = "file path:" + LogUtils.FILE_DOWNLOAD_PATH + fileName +"  receiver success \n";
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
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_RECEIVER_MESSAGE_SUCCESS,"host: "+new String(bytes)+"\n");
            }
        }
    }

    public boolean getHidStatus(){
        return mHidStatus;
    }

    public void closeUsbDevice(){
        try {
            if (mHidStatus) {
                mHidStatus = false;
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOpenUsbHidPort != null) {
                mOpenUsbHidPort.closeHidPort();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
