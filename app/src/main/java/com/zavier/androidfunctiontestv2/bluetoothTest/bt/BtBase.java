package com.zavier.androidfunctiontestv2.bluetoothTest.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.zavier.androidfunctiontestv2.customUtils.DataPacketUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    public static final UUID SPP_UUID = UUID.fromString("00001111-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BtBase";
    private static final int FLAG_MSG               = 0;  //消息标记
    private static final int FLAG_FILE              = 1;  //文件接收开始标记
    private static final int FLAG_FILE_MIDDLE       = 2;  //文件接收标记
    private static final int FLAG_FILE_END          = 3;  //文件接收结束标记

    private BluetoothSocket mSocket;
    private DataOutputStream mOut;
    private static final int mSize = 1024*4;
    private Listener mListener;
    private boolean isRead;
    private boolean isSending;
    private String mFileName;
    private int mFileStatus = 0;
    private String mSendType = null;

    public BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(BluetoothSocket socket) {
        mSocket = socket;
        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            final DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead) { //死循环读取
                switch (in.readInt()) {
                    case FLAG_MSG: //读取短消息
                        String msg = in.readUTF();
                        notifyUI(Listener.RECEIVE_MSG, msg);
                        break;
                    case FLAG_FILE:
                        try {
                            mFileName = in.readUTF();
                            long fileLen = in.readLong(); //文件长度
                            LogUtils.LogD(TAG, "loopRead FLAG_FILE fileName:"+mFileName+" fileLen:"+fileLen);
                            File file = new File(LogUtils.FILE_DOWNLOAD_PATH + mFileName);
                            if(file.exists()){
                                LogUtils.LogD(TAG, "loopRead file is exists,do delete");
                                file.delete();
                            }
                            notifyUI(Listener.RECEIVE_FILE_START, "正在接收文件(" + LogUtils.FILE_DOWNLOAD_PATH +mFileName+")\n");
                            // 读取文件内容
                            long len = 0;
                            int r;
                            byte[] bytes = new byte[mSize];
                            FileOutputStream out = new FileOutputStream(file);
                            while ((r = in.read(bytes)) != -1) {
                                out.write(bytes, 0, r);
                                Arrays.fill(bytes, (byte) 0);
                                len += r;
                                if (len >= fileLen){
                                    notifyUI(Listener.RECEIVE_FILE_END,
                                            "文件接收完成(" + LogUtils.FILE_DOWNLOAD_PATH +mFileName+")" + "\n");
                                    break;
                                }
                            }
                            break;
                        } catch (IOException e) {
                            LogUtils.LogD(TAG,"loopRead receive error");
                            e.printStackTrace();
                        }
                        break;
                    default:
                        LogUtils.LogD(TAG,"loopRead flag default");
                        break;
                }
            }
        } catch (Throwable e) {
            close();
        }
    }

    public static void createFileWithByte(byte[] bytes, String filePath, String fileName, int size) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(filePath, fileName);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file,true);
//            if (type == 1) {
//                outputStream.write(bytes, 0, size);
//            } else {
//                outputStream.write(bytes);
//            }
            outputStream.write(bytes, 0, size);
            //outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        }
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg) {
        if (checkSend()) return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.SEND_MSG, msg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    /**
     * 发送文件
     */
    public void sendFile(final String msg) {
        if (checkSend()) {
            return;
        }
        if(msg.isEmpty()){
            return ;
        }
        isSending = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(msg);
                    if(!file.exists()){
                        LogUtils.LogD(TAG,"sendFile file is not exists");
                        isSending = false;
                        return;
                    }
                    notifyUI(Listener.SEND_FILE_START, "正在发送文件(" + msg + ")····················\n");
                    FileInputStream in = new FileInputStream(msg);
                    int r, count = 0;
                    byte[] b = new byte[mSize];
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    while ((r = in.read(b)) != -1) {
                        count += r;
                        mOut.write(b, 0, r);
                    }
                    notifyUI(Listener.SEND_FILE_END, "文件发送完成.\n");
                } catch (Throwable e) {
                    close();
                }
                isSending = false;
            }
        });
        thread.start();
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            LogUtils.LogD(TAG, "socket close");
            isRead = false;
            if(mSocket != null){
                mSocket.close();
            }
            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    // ============================================通知UI===========================================================
    private boolean checkSend() {
        if (isSending) {
           // BlueToothAdapter.toast("正在发送其它数据,请稍后再发...", 0);
            return true;
        }
        return false;
    }

    private void notifyUI(final int state, final Object obj) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null) {
                        mListener.socketNotify(state, obj);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public interface Listener {
        int DISCONNECTED                = 0;
        int CONNECTED                   = 1;
        int RECEIVE_MSG                 = 2;
        int SEND_MSG                    = 3;
        int RECEIVE_FILE_START          = 4;
        int RECEIVE_FILE_MIDDLE         = 5;
        int RECEIVE_FILE_END            = 6;
        int SEND_FILE_START             = 7;
        int SEND_FILE_END               = 8;
        void socketNotify(int state, Object obj);
    }
}
