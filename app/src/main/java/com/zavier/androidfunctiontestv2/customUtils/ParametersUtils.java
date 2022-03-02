package com.zavier.androidfunctiontestv2.customUtils;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.testActivity.AudioActivity;
import com.zavier.androidfunctiontestv2.testActivity.BluetoothActivity;
import com.zavier.androidfunctiontestv2.testActivity.BluetoothFileActivity;
import com.zavier.androidfunctiontestv2.testActivity.BrightnessActivity;
import com.zavier.androidfunctiontestv2.testActivity.CameraActivity;
import com.zavier.androidfunctiontestv2.testActivity.DisplayActivity;
import com.zavier.androidfunctiontestv2.testActivity.FingerPrintActivity;
import com.zavier.androidfunctiontestv2.testActivity.HardwareInfoActivity;
import com.zavier.androidfunctiontestv2.testActivity.NetworkActivity;
import com.zavier.androidfunctiontestv2.testActivity.RecordActivity;
import com.zavier.androidfunctiontestv2.testActivity.SensorActivity;
import com.zavier.androidfunctiontestv2.testActivity.SerialPortActivity;
import com.zavier.androidfunctiontestv2.testActivity.SystemInterfaceActivity;
import com.zavier.androidfunctiontestv2.testActivity.TouchActivity;
import com.zavier.androidfunctiontestv2.testActivity.TouchPenActivity;
import com.zavier.androidfunctiontestv2.testActivity.UsbCommunicationActivity;
import com.zavier.androidfunctiontestv2.testActivity.VideoActivity;
import com.zavier.androidfunctiontestv2.testActivity.WifiActivity;

import java.util.Arrays;
import java.util.List;

public class ParametersUtils {
    private static final String TAG = "ParametersUtils";
    public static final int TEST_DISPLAY_COLOR              = 0;
    public static final int TEST_BRIGHTNESS                 = 1;
    public static final int TEST_DISPLAY_TOUCH              = 2;
    public static final int TEST_DISPLAY_PEN                = 3;
    public static final int TEST_AUDIO                      = 4;
    public static final int TEST_RECORD                     = 5;
    public static final int TEST_CAMERA                     = 6;
    public static final int TEST_USB_COMMUNICATION          = 7;
    public static final int TEST_BLUETOOTH                  = 8;
    public static final int TEST_BLUETOOTH_FILE             = 9;
    public static final int TEST_WIFI                       = 10;
    public static final int TEST_NETWORK                    = 11;
    public static final int TEST_VIDEO                      = 12;
    public static final int TEST_SENSOR                     = 13;
    public static final int TEST_SERIAL_PORT                = 14;
    public static final int TEST_SYSTEM_INTERFACE           = 15;
    public static final int TEST_HARDWARE_INFO              = 16;

    public static final int TEST_FINGER_PRINT = -1;

    public static final int TYPE_MAIN_FUNC = 0;
    public static final int TYPE_SENSOR_TEST = 1;

    private static final String[] mFuncName = {
            "display",          //0
            "brightness",
            "touch",
            "pen",
            "audio",
            "record",
            "camera",
            "usb_communication",
            "bluetooth",
            "bluetooth_file",
            "wifi",         //10
            "network",
            "video",
            "sensor",
            "serial_port",
            "system_interface",
            "hardware_info",
            };

    private static final Integer[] mFuncNameId = {
            R.string.main_func_display,     //0
            R.string.main_func_brightness,
            R.string.main_func_touch,
            R.string.main_func_pen,
            R.string.main_func_audio,
            R.string.main_func_record,
            R.string.main_func_camera,
            R.string.main_func_usb_communication,
            R.string.main_func_bluetooth,
            R.string.main_func_bluetooth_file,
            R.string.main_func_wifi,          //10
            R.string.main_func_network,
            R.string.main_func_video,
            R.string.main_func_sensor,
            R.string.main_func_serial_port,
            R.string.main_func_system_interface,
            R.string.main_func_hardware_info,
    };

    private static final Class<?>[] mFuncClass = {
            DisplayActivity.class,          //0
            BrightnessActivity.class,
            TouchActivity.class,
            TouchPenActivity.class,
            AudioActivity.class,
            RecordActivity.class,
            CameraActivity.class,
            UsbCommunicationActivity.class,
            BluetoothActivity.class,
            BluetoothFileActivity.class,
            WifiActivity.class,             //10
            NetworkActivity.class,
            VideoActivity.class,
            SensorActivity.class,
            SerialPortActivity.class,
            SystemInterfaceActivity.class,
            HardwareInfoActivity.class,
            };

    public static final List<String> funcNameList = Arrays.asList(mFuncName);

    public static final List<Class<?>> funcClassList = Arrays.asList(mFuncClass);

    public static final List<Integer> funcNameIdList = Arrays.asList(mFuncNameId);

    public static final int TEST_MAX  = funcClassList.size();

    public static int getClassId(Class<?> c){
        for(int i = 0; i < TEST_MAX; i++){
            if(c.equals(funcClassList.get(i))){
                LogUtils.LogD(TAG, "getClassId : " + i);
                return i;
            }
        }
        return -1;
    }

}
