package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

public class SlaveReceiver extends BroadcastReceiver {

    private OpenSlaveListener mOpenDevicesListener;
    private OnUsbStatusChanged mOnUSBConnStatusChanged;
    private static final String TAG = "SlaveReceiver";

    public SlaveReceiver(OpenSlaveListener openDevicesListener, OnUsbStatusChanged onUsbStatusChanged) {
        mOpenDevicesListener = openDevicesListener;
        mOnUSBConnStatusChanged = onUsbStatusChanged;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"action:"+action);

        if(MessageUtils.USB_ACTION.equals(action)) {
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (usbDevice != null) {
                    mOpenDevicesListener.SlaveOpenAccessoryModel(usbDevice);
                } else {
                    mOpenDevicesListener.SlaveOpenAccessoryError();
                }
            } else {
                mOpenDevicesListener.SlaveOpenAccessoryError();
            }
        } else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED) || action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)){
            mOnUSBConnStatusChanged.onUsbDetached();
        } else if(action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) || action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
            mOnUSBConnStatusChanged.onUsbAttached();
        }


    }

    public interface OpenSlaveListener {
        /**
         * 打开Accessory模式
         *
         * @param usbDevice
         */
        void SlaveOpenAccessoryModel(UsbDevice usbDevice);

        /**
         * 打开设备(手机)失败
         */
        void SlaveOpenAccessoryError();
    }
}
