package com.zavier.androidfunctiontestv2.customUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.zavier.androidfunctiontestv2.testActivity.BluetoothActivity;
import com.zavier.androidfunctiontestv2.testActivity.SystemInterfaceActivity;
import com.zavier.androidfunctiontestv2.testActivity.WifiActivity;

public class ReceiverUtils extends BroadcastReceiver {
    private static final String TAG = "ReceiverUtils";
    private Handler mHandler = null;
    private Class<?> mClass = null;

    public ReceiverUtils(Handler handler, Class<?> Class){
        this.mHandler = handler;
        this.mClass = Class;
    }

    public ReceiverUtils(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.LogD(TAG,"action: " + action);
        if(mClass.equals(BluetoothActivity.class)){
            doBluetooth(intent);
        } else if(mClass.equals(WifiActivity.class)){
            doWifi(intent);
        } else if(mClass.equals(SystemInterfaceActivity.class)){
            doSystemInterface(intent);
        } else {
            LogUtils.LogE(TAG, "class is null");
        }
    }

    private void doBluetooth(Intent intent){
        int arg1 = 0;
        int arg2 = 0;
        int what = 0;
        Object obj = null;
        String action = intent.getAction();
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            arg1 = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, Integer.MIN_VALUE);
            what = MessageUtils.MSG_BLUETOOTH_STATE_CHANGED;

        } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
            what = MessageUtils.MSG_BLUETOOTH_DISCOVERY_STARTED;

        } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
            what = MessageUtils.MSG_BLUETOOTH_DISCOVERY_FINISHED;

        } else if(action.equals(BluetoothDevice.ACTION_FOUND)){
            what = MessageUtils.MSG_BLUETOOTH_FOUND;
            obj = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }
        MessageUtils.sendMessage(mHandler, what, arg1, arg2, obj);
    }

    private void doWifi(Intent intent){
        int arg1 = 0;
        int arg2 = 0;
        int what = 0;
        Object obj = null;
        String action = intent.getAction();
        if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            what = MessageUtils.MSG_WIFI_STATE_CHANGED;
            arg1 = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

        } else if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
            what = MessageUtils.MSG_SUPPLICANT_CONNECTION_CHANGE;
            obj = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);

        } else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            what = MessageUtils.MSG_NETWORK_STATE_CHANGED;
            obj =  intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        } else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
            what = MessageUtils.MSG_SCAN_RESULTS_AVAILABLE;
        }

        MessageUtils.sendMessage(mHandler, what, arg1, arg2, obj);
    }

    private void doSystemInterface(Intent intent){
        int arg1 = 0;
        int arg2 = 0;
        int what = 0;
        Object obj = null;
        String action = intent.getAction();
        String path = intent.getData().getPath();
        //intent.getAction());获取存储设备当前状态
        LogUtils.LogD(TAG,"doSystemInterface action:"+action);
        //intent.getData().getPath());获取存储设备路径
        LogUtils.LogD(TAG,"doSystemInterface path:"+path);
        if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
            what = MessageUtils.MSG_MEDIA_MOUNTED;
            obj = path;
        } else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
            what = MessageUtils.MSG_MEDIA_UNMOUNTED;
            obj = path;
        }
        MessageUtils.sendMessage(mHandler, what, arg1, arg2, obj);
    }

}
