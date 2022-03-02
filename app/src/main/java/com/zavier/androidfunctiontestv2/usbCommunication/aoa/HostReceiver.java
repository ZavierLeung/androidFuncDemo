package com.zavier.androidfunctiontestv2.usbCommunication.aoa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

public class HostReceiver extends BroadcastReceiver {

    private OpenHostListener mOpenAccessoryListener;
    private OnUsbStatusChanged mOnUSBConnStatusChanged;
    private static final String TAG = "HostReceiver";

    public HostReceiver(OpenHostListener openHostListener, OnUsbStatusChanged onUSBConnStatusChanged) {
        mOpenAccessoryListener = openHostListener;
        mOnUSBConnStatusChanged = onUSBConnStatusChanged;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG,"action:"+action);

        if(MessageUtils.USB_ACTION.equals(action)){
            UsbAccessory usbAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                if (usbAccessory != null) {
                    mOpenAccessoryListener.hostOpenAccessoryModel(usbAccessory);
                } else {
                    mOpenAccessoryListener.hostOpenAccessoryError();
                }
            } else {
                mOpenAccessoryListener.hostOpenAccessoryError();
            }
        }  else if(action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED) || action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)){
            mOnUSBConnStatusChanged.onUsbDetached();
        } else if(action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) || action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
            mOnUSBConnStatusChanged.onUsbAttached();
        }
    }

    public interface OpenHostListener {
        /**
         * 打开Accessory模式
         *
         * @param usbAccessory
         */
        void hostOpenAccessoryModel(UsbAccessory usbAccessory);

        /**
         * 打开设备(手机)失败
         */
        void hostOpenAccessoryError();
    }
}
