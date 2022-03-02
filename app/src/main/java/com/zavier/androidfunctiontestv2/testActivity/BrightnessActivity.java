package com.zavier.androidfunctiontestv2.testActivity;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.PermissionsUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.lang.reflect.Method;

public class BrightnessActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "BrightnessActivity";
    private TextViewTitleUtils mTitleUtile;
    private Button mRetestBtn;
    private SeekBar mSeekBar;
    private TextView mBarTv;
    private final int MAX_BRIGHTNESS = 255;
    private final int MIN_BRIGHTNESS = 5;
    private int mCurBrightness = -1;
    private static final int MSG_TEST_BRIGHTNESS = 0;
    private static final int ONE_STAGE = 5;
    private static int mCount = 0;
    private int mBrightness = 30;
    private boolean increase = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mHandler.sendEmptyMessage(MSG_TEST_BRIGHTNESS);
        initCurBrightness();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mHandler.removeMessages(MSG_TEST_BRIGHTNESS);
        initCurBrightness();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(BrightnessActivity.this);
        mTitleUtile.setTitle(R.string.main_func_brightness);

        mRetestBtn = findViewById(R.id.brightness_test_play_btn);
        mRetestBtn.setOnClickListener(this);
        mBarTv = findViewById(R.id.brightness_progress_bar_tv);
        mSeekBar = findViewById(R.id.brightness_progress_bar_seekbar);
        mSeekBar.setMax(MAX_BRIGHTNESS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSeekBar.setMin(MIN_BRIGHTNESS);
        }
        initCurBrightness();
        LogUtils.LogD(TAG, " initView cur brightness:"+mCurBrightness);
    }

    private void initCurBrightness(){
        mCurBrightness = getBrightness();
        setWindowBrightness(mCurBrightness);
        mSeekBar.setProgress(mCurBrightness);
        mBarTv.setText(mCurBrightness + "/"+ MAX_BRIGHTNESS);
    }

    private void changeBrightness(int brightness){
        setWindowBrightness(brightness);
        mSeekBar.setProgress(brightness);
        mBarTv.setText(brightness + "/"+ MAX_BRIGHTNESS);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.brightness_test_play_btn:
                mHandler.sendEmptyMessage(MSG_TEST_BRIGHTNESS);
                break;
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int delay = 25;
            if (msg.what == MSG_TEST_BRIGHTNESS) {
                if (increase) {
                    mBrightness += ONE_STAGE;
                    if (mBrightness >= MAX_BRIGHTNESS) {
                        mBrightness = MAX_BRIGHTNESS;
                        increase = false;
                        mCount++;
                        delay = 500;
                    }
                } else {
                    mBrightness -= ONE_STAGE;
                    if (mBrightness <= MIN_BRIGHTNESS) {
                        mBrightness = MIN_BRIGHTNESS;
                        increase = true;
                        mCount++;
                        delay = 500;
                    }
                }
                if(mCount == 2){
                    changeBrightness(mCurBrightness);
                    mCount = 0;
                } else {
                    changeBrightness(mBrightness);
                    sendEmptyMessageDelayed(MSG_TEST_BRIGHTNESS, delay);
                }
            }
        }
    };

    /**
     * 设置屏幕亮度
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_SETTINGS" />}</p>
     * 并得到授权
     *
     * @param brightness 亮度值
     */
    private boolean setOldBrightness(@IntRange(from = 0, to = 255) final int brightness) {
        ContentResolver resolver =  getContentResolver();
//        boolean b = Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        boolean b = PermissionsUtils.doWriteSettings(this, Settings.System.SCREEN_BRIGHTNESS,
                brightness);
        resolver.notifyChange(Settings.System.getUriFor("screen_brightness"), null);
        return b;
    }

    /**
     * 获取屏幕亮度
     *
     * @return 屏幕亮度 0-255
     */
    public int getBrightness() {
        try {
            return Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 需要系统签名才能调用
     *
     * */
    public void setBrightness(@IntRange(from = 0, to = 255) final int brightness) {
        try {
            Class<?> cServiceManager = Class.forName("android.os.ServiceManager");
            Method mGetService = cServiceManager.getMethod("getService",String.class);
            Object oPowerManagerService = mGetService.invoke(null, Context.POWER_SERVICE);
            Class<?> cIPowerManagerStub = Class.forName("android.os.IPowerManager$Stub");
            Method mAsInterface = cIPowerManagerStub.getMethod("asInterface", IBinder.class);
            Method mBrightness = cIPowerManagerStub.getMethod("setTemporaryScreenBrightnessSettingOverride",int.class);
            Object oIPowerManager = mAsInterface.invoke(null,oPowerManagerService);
            mBrightness.invoke(oIPowerManager,brightness);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 设置窗口亮度
     *
     * @param brightness 亮度值
     */
    public void setWindowBrightness(@IntRange(from = 0, to = 255) final int brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness / 255f;
        getWindow().setAttributes(lp);
    }

    /**
     * 获取窗口亮度
     *
     * @return 屏幕亮度 0-255
     */
    public int getWindowBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float brightness = lp.screenBrightness;
        if (brightness < 0) return getBrightness();
        return (int) (brightness * 255);
    }
}
