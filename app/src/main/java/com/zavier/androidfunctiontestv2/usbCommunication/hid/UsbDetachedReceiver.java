package com.zavier.androidfunctiontestv2.usbCommunication.hid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

public class UsbDetachedReceiver extends BroadcastReceiver {

    private UsbDetachedListener mUsbDetachedListener;

    public UsbDetachedReceiver(UsbDetachedListener usbDetachedListener) {
        mUsbDetachedListener = usbDetachedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.LogD("UsbDetachedReceiver","action: "+intent.getAction());

        if(intent.getAction().equals(MessageUtils.USB_STATE)){
            mUsbDetachedListener.usbHidState();
        } else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
            mUsbDetachedListener.usbHidDetached();
        } else if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
            mUsbDetachedListener.usbHidAttached();
        }
    }

    public interface UsbDetachedListener {
        /**
         * usb断开连接
         */
        void usbHidDetached();

        void usbHidAttached();

        void usbHidState();
    }
}
