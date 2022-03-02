package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import androidx.annotation.Nullable;


/**
 * 连接服务
 * 在独立线程中使用
 * Created by lidechen on 3/24/17.
 */

public class UsbStateService extends Service implements HostReceiver.OpenHostListener,
        SlaveReceiver.OpenSlaveListener, OnUsbStatusChanged {

    private static final String TAG = "UsbStateService";
    private ConnectBinder connectBinder = new ConnectBinder();
    private int mDeviceType = 0;
    private HostReceiver mHostReceiver;
    private SlaveReceiver mSlaveReceiver;
    private HostDevice hostDevice;
    private SlaveDevice slaveDevice;
    /**
     * 回调
     */
    private ServiceCallback serviceCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"enter onBind");
        return new ConnectBinder();
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"enter onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDeviceType = intent.getIntExtra(MessageUtils.USB_TYPE,0);
        Log.d(TAG,"enter onStartCommand, device type :"+ mDeviceType);

        if(mDeviceType == MessageUtils.USB_AOA_HOST){
            mHostReceiver = new HostReceiver(this, this);
            IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(MessageUtils.USB_STATE);
            filter.addAction(MessageUtils.USB_ACTION);
            registerReceiver(mHostReceiver, filter);
        } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY){
            mSlaveReceiver = new SlaveReceiver(this, this);
            IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(MessageUtils.USB_STATE);
            filter.addAction(MessageUtils.USB_ACTION);
            registerReceiver(mSlaveReceiver, filter);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 内部类继承Binder
     *
     */
    public class ConnectBinder extends Binder {
        /**
         * 声明方法返回值是UsbStateService本身
         * @return
         */
        public UsbStateService getService() {
            return UsbStateService.this;
        }
    }

    public void setHostDevice(HostDevice host){
        hostDevice = host;
    }

    public void setSlaveDevice(SlaveDevice slave){
        slaveDevice = slave;
    }
    
    /**
     * 外部初始化的回调函数
     *
     * */
    public void initServiceCallback(ServiceCallback callback){
        Log.d(TAG,"enter initServiceCallback");
        this.serviceCallback = callback;
    }

    /**
     * 回调接口
     *
     * @author lenovo
     *
     */
    public interface ServiceCallback {
        void hostOpenAoa(UsbAccessory usbAccessory);
        void hostOpenAoaError();
        void SlaveOpenAoa(UsbDevice usbDevice);
        void SlaveOpenAoaError();
        void UsbAttached(HostDevice hostDevice, SlaveDevice slaveDevice);
        void UsbDetached(HostDevice hostDevice, SlaveDevice slaveDevice);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHostReceiver != null){
            unregisterReceiver(mHostReceiver);
        }

        if(mSlaveReceiver != null){
            unregisterReceiver(mSlaveReceiver);
        }
    }

    @Override
    public void hostOpenAccessoryModel(UsbAccessory usbAccessory) {
        serviceCallback.hostOpenAoa(usbAccessory);
    }

    @Override
    public void hostOpenAccessoryError() {
        serviceCallback.hostOpenAoaError();
    }

    @Override
    public void SlaveOpenAccessoryModel(UsbDevice usbDevice) {
        serviceCallback.SlaveOpenAoa(usbDevice);
    }

    @Override
    public void SlaveOpenAccessoryError() {
        serviceCallback.SlaveOpenAoaError();
    }

    @Override
    public void onUsbAttached() {
        Log.d(TAG,"enter onUsbAttached");
        if(mDeviceType == MessageUtils.USB_AOA_HOST && hostDevice != null) {
            serviceCallback.UsbAttached(hostDevice, null);
        } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY && slaveDevice != null){
            serviceCallback.UsbAttached(null, slaveDevice);
        }
    }

    @Override
    public void onUsbDetached() {
        Log.d(TAG,"enter onUsbDetached");
        if(mDeviceType == MessageUtils.USB_AOA_HOST && hostDevice != null) {
            serviceCallback.UsbDetached(hostDevice, null);
        } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY && slaveDevice != null){
            serviceCallback.UsbDetached(null, slaveDevice);
        }
    }
}
