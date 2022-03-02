package com.zavier.androidfunctiontestv2.customUtils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.DataOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PermissionsUtils {
    private final static String TAG = "PermissionsUtils";

    private String[] permissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION};

    public PermissionsUtils(Context context) {
        doPermission(context);
    }

    public void doPermission(Context context) {
        // Android 6.0动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String str : permissions) {
                if (((Activity) context).checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    ((Activity) context).requestPermissions(permissions, 1);
                    break;
                }
            }
        }
    }

    public static boolean checkGetRootAuth() {
        Process process = null;
        DataOutputStream os = null;
        try {
            LogUtils.LogD(TAG, "to exec su");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            LogUtils.LogD(TAG, "exitValue=" + exitValue);
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LogUtils.LogD(TAG, "Unexpected error - Here is what I know: " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean doWriteSettings(Context context, String modeString, int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                return Settings.System.putInt(context.getContentResolver(), modeString, mode);
            }
        } else {
            return Settings.System.putInt(context.getContentResolver(), modeString, mode);
        }

        return false;
    }

    public static void setSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method setter = clazz.getDeclaredMethod("set", String.class, String.class);
            setter.invoke(clazz, property, defaultValue);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean getAppInfo(){
        if(getSystemProperty("ro.build.user").equals("thomas") &&
                getSystemProperty("ro.product.model").equals("rk3399-a5801")){
            return true;
        } else {
            LogUtils.LogD(TAG, "user: "+getSystemProperty("ro.build.user")+ " model:"+getSystemProperty("ro.product.model"));
            return false;
        }
    }



    public static String getSystemProperty(String property){
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)){
                return value;
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
