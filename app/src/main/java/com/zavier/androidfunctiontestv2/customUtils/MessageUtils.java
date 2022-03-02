package com.zavier.androidfunctiontestv2.customUtils;

import android.os.Handler;
import android.os.Message;

public class MessageUtils {
    private static final String TAG = "MessageUtils";

    public static final int FLAG_TEST_SUCCESS       = 0;
    public static final int FLAG_TEST_FAIL          = 1;
    public static final int FLAG_TEST_READY         = 2;

    public static final int MSG_BLUETOOTH_STATE_CHANGED         = 1;
    public static final int MSG_BLUETOOTH_DISCOVERY_STARTED     = 2;
    public static final int MSG_BLUETOOTH_DISCOVERY_FINISHED    = 3;
    public static final int MSG_BLUETOOTH_FOUND                 = 4;
    public static final int MSG_USB_CONNECTED_SUCCESS           = 10;
    public static final int MSG_USB_CONNECTED_FAILED            = 11;
    public static final int MSG_USB_CONNECTED_ERR               = 12;
    public static final int MSG_USB_RECEIVER_MESSAGE_SUCCESS    = 13;
    public static final int MSG_USB_SEND_MESSAGE_SUCCESS        = 14;
    public static final int MSG_USB_SEND_FILE_SUCCESS           = 15;
    public static final int MSG_USB_SENDING_FILE                = 16;
    public static final int MSG_USB_RECEIVER_FILE_SUCCESS       = 17;
    public static final int MSG_USB_RECEIVER_FILE_START         = 18;
    public static final int MSG_WIFI_STATE_CHANGED              = 21;
    public static final int MSG_SUPPLICANT_CONNECTION_CHANGE    = 22;
    public static final int MSG_NETWORK_STATE_CHANGED           = 23;
    public static final int MSG_SCAN_RESULTS_AVAILABLE          = 24;
    public static final int MSG_SERIAL_OPEN                     = 31;
    public static final int MSG_SERIAL_SEND                     = 32;
    public static final int MSG_SERIAL_RECEIVE                  = 33;
    public static final int MSG_MEDIA_MOUNTED                   = 41;
    public static final int MSG_MEDIA_UNMOUNTED                 = 42;
    public static final int MSG_PHYSICS_KEY                     = 43;
    public static final int MSG_SCREEN_OFF                      = 44;
    public static final int MSG_SCREEN_ON                       = 45;
    public static final int MSG_BLUETOOTH_CONNECT_SUCCESS       = 51;
    public static final int MSG_BLUETOOTH_CONNECT_FAILED        = 52;
    public static final int MSG_BLUETOOTH_CONNECT_SERVER        = 53;
    public static final int MSG_BLUETOOTH_MESSAGE_RECEIVE       = 54;
    public static final int MSG_BLUETOOTH_MESSAGE_SEND          = 55;
    public static final int MSG_BLUETOOTH_SERVER_START          = 56;

    public static final int USB_AOA_HOST           = 1;
    public static final int USB_AOA_ACCESSORY      = 2;
    public static final int USB_HID_HOST           = 3;
    public static final int USB_HID_DEVICE         = 4;

    public static final int RECEIVING = 0;
    public static final int RECEIVED_END = 1;

    public static final String USB_MODE = "usbMode";
    public static final String USB_HOST_MODE = "host";
    public static final String USB_SLAVE_MODE = "slave";
    public static final String USB_TYPE = "usbType";

    public static final String USB_ACTION = "com.zavier.androidfunctiontestv2.testActivity.UsbCommunicationActivity";
    public static final String USB_STATE = "android.hardware.usb.action.USB_STATE";

    public static void sendMessage(Handler handler, int what, Object obj){
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    public static void sendMessage(Handler handler, int what, int arg1, Object obj){
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    public static void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj){
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    public static  void sendMessage(Handler handler, Object obj){
        Message msg = Message.obtain();
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    public static  void sendEmptyMessage(Handler handler, int what){
        handler.sendEmptyMessage(what);
    }

}
