package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.ReceiverUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class SystemInterfaceActivity extends AppCompatActivity {
    private static final String TAG = "SystemInterfaceActivity";
    private TextViewTitleUtils mTitleUtile;
    private TextView mResultTv;
    private ReceiverUtils mReceiver,mPowerReceiver;
    private StorageManager mStorageManager;
    private static String mInterSD; // 内置sd卡
    private static String mExternalSD; // 外置sd卡 1
    private static String mUSB; // U盘，外置sd卡2
    private String[] mPath;
    private static int mInterSDFlag     = 0; // 内置sd卡
    private static int mExternalSDFlag  = 1; // 外置sd卡 1
    private static int mUSBFlag         = 2; // U盘，外置sd卡2
    public final MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_interface);
        initView();
        getStorageList();
        initReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.LogD(TAG, "onDestroy");
        if(mReceiver != null){
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                LogUtils.LogE(TAG,"unregisterReceiver interfaceReceiver failure :" + e.getCause());
            }
        }
        if(mPowerReceiver != null){
            try {
                unregisterReceiver(mPowerReceiver);
            } catch (Exception e) {
                LogUtils.LogE(TAG,"unregisterReceiver interfaceReceiver failure :" + e.getCause());
            }
        }
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this,true,false);
        mTitleUtile.setTitle(R.string.main_func_system_interface);
        mTitleUtile.setSubTitle(R.string.system_interface_sub_title);
        mResultTv = findViewById(R.id.system_interface_result_tv);
    }

    public void getStorageList() {
        mStorageManager = (StorageManager) getSystemService(Activity.STORAGE_SERVICE);
        try {
            Method methodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
            mPath = (String[]) methodGetPaths.invoke(mStorageManager);
            if (mPath.length > 0) {
                mInterSD= mPath[0];
                LogUtils.LogD(TAG, "mInterSD is " + mInterSD);
            }
            if (mPath.length > 1) {
                mExternalSD= mPath[1];
                LogUtils.LogD(TAG, "mExternalSD is " + mExternalSD);
            }
            if (mPath.length > 2) {
                mUSB= mPath[2];
                LogUtils.LogD(TAG, "mUSB is " + mUSB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initReceiver(){
        mReceiver = new ReceiverUtils(handler, this.getClass());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);//如果SDCard未安装,并通过USB大容量存储共享返回
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);//表明sd对象是存在并具有读/写权限
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//SDCard已卸掉,如果SDCard是存在但没有被安装
        filter.addAction(Intent.ACTION_MEDIA_CHECKING); //表明对象正在磁盘检查
        filter.addAction(Intent.ACTION_MEDIA_EJECT); //物理的拔出 SDCARD
        filter.addAction(Intent.ACTION_MEDIA_REMOVED); //完全拔出
        filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
        registerReceiver(mReceiver, filter);

        IntentFilter filter1 = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter1.addAction(Intent.ACTION_SCREEN_ON);
        // Power
        registerReceiver(mBatInfoReceiver, filter1);
    }

    private int getTypePath(String path){
        int count = stringNumber(path,"/");
        if(count == 2){
            if(path.equals(mExternalSD)){
                return mExternalSDFlag;
            } else if(path.equals(mUSB)){
                return mUSBFlag;
            }
        } else if(count > 2){
            String srcString = path.substring(0, stringIndex(path, "/") - 1);
            String sdString = null;
            String usbString = null;
            if(!mExternalSD.isEmpty()){
                sdString = mExternalSD.substring(0, stringIndex(mExternalSD, "/") - 1);
            }
            if(!mUSB.isEmpty()){
                usbString = mUSB.substring(0, stringIndex(mUSB, "/") - 1);
            }
            Log.d("interfacetest","usbString:"+usbString);
            Log.d("interfacetest","srcString:"+srcString);
            Log.d("interfacetest","sdString:"+sdString);
            if(srcString.equals(sdString)){
                return mExternalSDFlag;
            } else if(srcString.equals(usbString)){
                return mUSBFlag;
            }
        }
        return mInterSDFlag;
    }

    public int stringNumber(String srcText, String findText) {
        int count = 0;
        int index = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            count++;
        }
        return count;
    }

    public int stringIndex(String srcText, String findText) {
        int index = 0;
        int ret = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            ret = index;
        }
        return ret;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MessageUtils.sendMessage(handler,MessageUtils.MSG_PHYSICS_KEY,keyCode,null);
        if(keyCode == KeyEvent.FLAG_KEEP_TOUCH_MODE){
            return super.onKeyDown(keyCode, event);
        } else {
            return true;
        }
    }

    public String parseKeyCode(int keyCode) {
        String ret = "";
        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
                // 监控/拦截/屏蔽电源键 这里拦截不了
                ret = "get Key KEYCODE_POWER(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
                // 监控/拦截/屏蔽返回键
                ret = "get Key KEYCODE_RIGHT_BRACKET";
                break;
            case KeyEvent.KEYCODE_MENU:
                // 监控/拦截菜单键
                ret = "get Key KEYCODE_MENU";
                break;
            case KeyEvent.KEYCODE_HOME:
                // 由于Home键为系统键，此处不能捕获
                ret = "get Key KEYCODE_HOME";
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                // 监控/拦截/屏蔽上方向键
                ret = "get Key KEYCODE_DPAD_UP";
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                // 监控/拦截/屏蔽左方向键
                ret = "get Key KEYCODE_DPAD_LEFT";
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // 监控/拦截/屏蔽右方向键
                ret = "get Key KEYCODE_DPAD_RIGHT";
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // 监控/拦截/屏蔽下方向键
                ret = "get Key KEYCODE_DPAD_DOWN";
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // 监控/拦截/屏蔽中方向键
                ret = "get Key KEYCODE_DPAD_CENTER";
                break;
            case KeyEvent.FLAG_KEEP_TOUCH_MODE:
                // 监控/拦截/屏蔽长按
                ret = "get Key FLAG_KEEP_TOUCH_MODE";
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // 监控/拦截/屏蔽下方向键
                ret = "get Key KEYCODE_VOLUME_DOWN(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                // 监控/拦截/屏蔽中方向键
                ret = "get Key KEYCODE_VOLUME_UP(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_BRIGHTNESS_DOWN:
                // case KeyEvent.KEYCODE_BRIGHTNESS_DOWN:
                // 监控/拦截/屏蔽亮度减键
                ret = "get Key KEYCODE_BRIGHTNESS_DOWN(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_BRIGHTNESS_UP:
                // case KeyEvent.KEYCODE_BRIGHTNESS_UP:
                // 监控/拦截/屏蔽亮度加键
                ret = "get Key KEYCODE_BRIGHTNESS_UP(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                ret = "get Key KEYCODE_MEDIA_PLAY(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                ret = "get Key KEYCODE_MEDIA_PAUSE(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                ret = "get Key KEYCODE_MEDIA_PREVIOUS(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                ret = "get Key KEYCODE_MEDIA_PLAY_PAUSE(KeyCode:" + keyCode + ")";
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                ret = "get Key KEYCODE_MEDIA_NEXT(KeyCode:" + keyCode + ")";
                break;
            default:
                ret = "keyCode: "
                        + keyCode
                        + " (http://developer.android.com/reference/android/view/KeyEvent.html)";
                break;
        }
        return ret;
    }

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            int what = 0;
            Object obj = null;
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                what = MessageUtils.MSG_SCREEN_OFF;
                obj = "get Key KEYCODE_POWER(KeyCode:26)-OFF";
            } else if(Intent.ACTION_SCREEN_ON.equals(action)){
                what = MessageUtils.MSG_SCREEN_ON;
                obj = "get Key KEYCODE_POWER(KeyCode:26)-ON";
            }
            MessageUtils.sendMessage(handler, what, obj);
        }
    };

    //最标准的写法,避免了Activity被用户关闭之后,异步消息还未处理完毕,造成内存泄露
    static class MyHandler extends Handler{
        WeakReference<SystemInterfaceActivity> weakReference;
        public MyHandler(SystemInterfaceActivity activity) {
            weakReference = new WeakReference<SystemInterfaceActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference.get() != null) {
                int flag = -1;
                switch (msg.what){
                    case MessageUtils.MSG_MEDIA_MOUNTED:
                        weakReference.get().getStorageList();
                        flag = weakReference.get().getTypePath(msg.obj.toString());
                        if(flag == mExternalSDFlag){
                            weakReference.get().mResultTv.setText(" sdcard mounted");
                        } else if(flag == mUSBFlag){
                            weakReference.get().mResultTv.setText(" Usb mounted");
                        }
                        break;
                    case MessageUtils.MSG_MEDIA_UNMOUNTED:
                        weakReference.get().getStorageList();
                        flag = weakReference.get().getTypePath(msg.obj.toString());
                        if(flag == mExternalSDFlag){
                            weakReference.get().mResultTv.setText(" sdcard unmounted");
                        } else if(flag == mUSBFlag){
                            weakReference.get().mResultTv.setText(" Usb unmounted");
                        }
                        break;
                    case MessageUtils.MSG_SCREEN_OFF:
                    case MessageUtils.MSG_SCREEN_ON:
                        weakReference.get().mResultTv.setText(msg.obj.toString());
                        break;
                    case MessageUtils.MSG_PHYSICS_KEY:
                        weakReference.get().mResultTv.setText(weakReference.get().parseKeyCode(msg.arg1));
                        break;
                }
            }
        }
    }
}
