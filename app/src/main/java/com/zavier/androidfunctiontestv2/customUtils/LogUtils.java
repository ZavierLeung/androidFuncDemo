package com.zavier.androidfunctiontestv2.customUtils;

import android.os.Environment;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LogUtils {
    private static final boolean DEBUG = true;
    public static final String DATA_PATH = "/data/data/com.zavier.androidgwfunctiontest/";
    public final static String DATA_2_PATH = Environment.getExternalStorageDirectory().getPath() + "/Download/";
    public final static String NETWORK_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Download/";
    public final static String NETWORK_STATUS_FILE = Environment.getExternalStorageDirectory().getPath() + "/Download/NetworkStatus.txt";
    public final static String NETWORK_PING_FILE = Environment.getExternalStorageDirectory().getPath() + "/Download/NetworkPingLog.txt";
    public final static String FILE_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/Download/";

    public static void LogD(String tag, String logString) {
        if(DEBUG)
            Log.d(tag, logString);
    }

    public static void LogI(String tag, String logString) {
        Log.i(tag, logString);
    }

    public static void LogE(String tag, String logString) {
        Log.e(tag, logString);
    }

    public static  String getTimeDateFormat(){
        long ms = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return sdf.format(ms);
    }
}
