package com.zavier.androidfunctiontestv2.bluetoothTest.bt;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.util.concurrent.ExecutorService;

/**
 * 服务端监听和连接线程，只连接一个设备
 */
public class BtServer extends BtBase {
    private static final String TAG = BtServer.class.getSimpleName();
    private BluetoothServerSocket mSSocket;
    private Handler mHandler;

    public BtServer(Listener listener, Handler handler) {
        super(listener);
        mHandler = handler;
        listen();
    }

    /**
     * 监听客户端发起的连接
     */
    public void listen() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG, SPP_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BluetoothSocket socket = mSSocket.accept(); // 监听连接
                        mSSocket.close(); // 关闭监听，只连接一个设备
                        loopRead(socket); // 循环读取
                    } catch (Throwable e) {
                        close();
                    }
                }
            });
            thread.start();
            MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_SERVER_START,
                    "服务端[ "+adapter.getAddress()+" ]");
        } catch (Throwable e) {
            close();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            mSSocket.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}