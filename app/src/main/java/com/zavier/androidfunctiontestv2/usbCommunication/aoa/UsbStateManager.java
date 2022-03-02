package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;

import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.BIND_AUTO_CREATE;

public class UsbStateManager {
    private static final String TAG = "UsbStateManager";

    private Context mContext;
    private int mDeviceType;
    private Handler mHandler;
    private SlaveDevice mSlaveDevice;
    private HostDevice mHostDevice;
    private UsbStateService.ConnectBinder mConnectBinder;
    private ExecutorService mThreadPool;
    private UsbManager mUsbManager;
    private UsbStatus mUsbStatus;

    public UsbStateManager(Context context, Handler handler, int deviceType, UsbStatus usbStatus){
        this.mContext = context;
        this.mHandler = handler;
        this.mDeviceType = deviceType;
        this.mUsbStatus = usbStatus;
        initDevice();
        initUsbStateService();
    }

    private void initUsbStateService(){
        if(mDeviceType == MessageUtils.USB_AOA_HOST){
            Intent hostIntent = new Intent(mContext, UsbStateService.class);
            hostIntent.putExtra(MessageUtils.USB_TYPE, MessageUtils.USB_AOA_HOST);
            mContext.startService(hostIntent);
        } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY){
            Intent slaveIntent = new Intent(mContext, UsbStateService.class);
            slaveIntent.putExtra(MessageUtils.USB_TYPE, MessageUtils.USB_AOA_ACCESSORY);
            mContext.startService(slaveIntent);
        }
        Intent bindService = new Intent(mContext, UsbStateService.class);
        mContext.bindService(bindService, mConnection, BIND_AUTO_CREATE);
    }

    private void initDevice(){
        mThreadPool = Executors.newFixedThreadPool(5);
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        if(mDeviceType == MessageUtils.USB_AOA_HOST){
            mHostDevice = new HostDevice(mContext, mHandler, mThreadPool, mUsbManager);
        } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY){
            mSlaveDevice = new SlaveDevice(mContext, mHandler, mThreadPool, mUsbManager);
        }
    }

    public void sendMessageToHost(String messageContent){
        LogUtils.LogD(TAG,"sendMessageToHost message TYPE_SLAVE_DEVICE");
        if(mSlaveDevice == null){
            LogUtils.LogD(TAG,"sendMessageToHost mSlaveDevice is null");
            return ;
        }
        mSlaveDevice.sendMessageToHost(messageContent);
    }

    public void sendFileToHost(String filePath, int size){
        if(mSlaveDevice == null){
            LogUtils.LogD(TAG,"sendFileToHost mSlaveDevice is null");
            return ;
        }
        mSlaveDevice.sendFileToHost(filePath, size);
    }

    public void sendMessageToSlave(String messageContent){
        mHostDevice.sendMessageToSlave(messageContent);
    }

    public void sendFileToSlave(String filePath, int size){
        mHostDevice.sendFileToSlave(filePath, size);
    }

    public void stopService(){
        mContext.unbindService(mConnection);
        if(mDeviceType == MessageUtils.USB_AOA_HOST){
            Intent hostIntent = new Intent(mContext, UsbStateService.class);
            mContext.stopService(hostIntent);
        } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY){
            Intent slaveIntent = new Intent(mContext, UsbStateService.class);
            mContext.stopService(slaveIntent);
        }
    }

    public void closeUsbManager(){
        if(mHostDevice != null){
            mHostDevice.closeHostDevice();
        }
        if(mSlaveDevice != null){
            mSlaveDevice.closeSlaveDevice();
        }
        mThreadPool.shutdownNow();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        /**
         * 服务解除绑定时候调用
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        /**
         * 绑定服务的时候调用
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mConnectBinder = (UsbStateService.ConnectBinder) service;
            UsbStateService usbStateService = mConnectBinder.getService();
            if(mHostDevice != null){
                usbStateService.setHostDevice(mHostDevice);
            } else if(mSlaveDevice != null){
                usbStateService.setSlaveDevice(mSlaveDevice);
            }
            usbStateService.initServiceCallback(new UsbStateService.ServiceCallback() {
                @Override
                public void hostOpenAoa(UsbAccessory usbAccessory) {
                    LogUtils.LogD(TAG, "enter hostOpenAoa");
                    mHostDevice.setHostUsbConnect(true);
                    mHostDevice.initHostAccessory(usbAccessory);
                }

                @Override
                public void hostOpenAoaError() {
                    LogUtils.LogD(TAG, "enter hostOpenAoaError");

                }

                @Override
                public void SlaveOpenAoa(UsbDevice usbDevice) {
                    LogUtils.LogD(TAG, "enter SlaveOpenAoa");
                    mSlaveDevice.initSlaveAccessory(usbDevice);
                }

                @Override
                public void SlaveOpenAoaError() {
                    LogUtils.LogD(TAG, "enter SlaveOpenAoaError");

                }

                @Override
                public void UsbAttached(HostDevice hostDevice,SlaveDevice slaveDevice) {
                    LogUtils.LogD(TAG, "enter UsbAttached");
                    if(mDeviceType == MessageUtils.USB_AOA_HOST && hostDevice != null){
                        LogUtils.LogD(TAG, "enter checkHostAccessory");
                        hostDevice.checkHostAccessory();
                    } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY && slaveDevice != null){
                        LogUtils.LogD(TAG, "enter checkSlaveAccessory");
                        slaveDevice.checkSlaveAccessory();
                    }
                }

                @Override
                public void UsbDetached(HostDevice hostDevice,SlaveDevice slaveDevice) {
                    LogUtils.LogD(TAG, "enter UsbDetached");
                    if(mDeviceType == MessageUtils.USB_AOA_HOST && hostDevice != null){
                        LogUtils.LogD(TAG, "enter closeHostDevice");
                        hostDevice.setHostUsbConnect(false);
                        //hostDevice.closeHostDevice();
                        mUsbStatus.usbDetached();
                    } else if(mDeviceType == MessageUtils.USB_AOA_ACCESSORY && slaveDevice != null){
                        LogUtils.LogD(TAG, "enter closeSlaveDevice");
                        slaveDevice.closeSlaveDevice();
                    }
                }
            });
        }
    };

    public interface UsbStatus{
        void usbDetached();
    }
}
