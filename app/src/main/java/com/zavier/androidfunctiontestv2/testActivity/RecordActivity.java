package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.RecorderUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;
import com.zavier.androidfunctiontestv2.customView.LevelMeterView;

import java.io.File;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RecordActivity";
    private LevelMeterView mLevelMeterView;
    private Button mRetestBtn;
    private TextViewTitleUtils mTitleUtile;
    private RecorderUtils mRecorder;
    private AudioManager mAudioManager;
    private final static String ERRMSG = "Record error";
    private final static int RECORD_TIME = 5;
    private static final int MSG_TEST_MIC_ING = 8738;
    private static final int MSG_TEST_MIC_OVER = 13107;
    private static final int MSG_TEST_MIC_START = 4369;
    private boolean isSDcardTestOk = false;
    private Handler mHandler;
    private int mOldVolume;
    private int mTimes;
    boolean mSpeakerOn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        this.mHandler = new MyHandler();
        initView();
    }

    private void initView(){
        new ControlButtonUtils(RecordActivity.this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(RecordActivity.this,true,true);
        mTitleUtile.setTitle(R.string.main_func_record);
        mTitleUtile.setSubTitle(R.string.record_test_play);

        mLevelMeterView = findViewById(R.id.record_meter_view);
        mRetestBtn = findViewById(R.id.record_retest_btn);
        mRetestBtn.setOnClickListener(this);
        mRetestBtn.setEnabled(false);

        mRecorder = new RecorderUtils();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mLevelMeterView.setRecorder(mRecorder);
    }

    @Override
    protected void onResume() {

        super.onResume();

        this.isSDcardTestOk = false;
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            this.mTitleUtile.setResult(R.string.InsertSdCard);
            return;
        }

        if (!isSDcardHasSpace()) {
            this.mTitleUtile.setResult(R.string.SdCardNospace);
            stopMediaPlayBack();
            return;
        }
        stopMediaPlayBack();
        this.isSDcardTestOk = true;

        this.mOldVolume = this.mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = this.mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                maxVolume, 0);

        this.mSpeakerOn = mAudioManager.isSpeakerphoneOn();

        if (!this.mSpeakerOn) {
            this.mAudioManager.setSpeakerphoneOn(true);
        }
        this.mHandler.sendEmptyMessage(MSG_TEST_MIC_START);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.isSDcardTestOk) {
            switch (this.mRecorder.state()) {
                case RecorderUtils.IDLE_STATE:
                    this.mRecorder.delete();
                    break;
                case RecorderUtils.PLAYING_STATE:
                    this.mRecorder.stop();
                    this.mRecorder.delete();
                    break;
                case RecorderUtils.RECORDING_STATE:
                    this.mRecorder.stop();
                    this.mRecorder.clear();
                    break;
            }
            mAudioManager.setStreamVolume(3, mOldVolume, 0);
            if (mSpeakerOn) {
                mAudioManager.setSpeakerphoneOn(false);
            }
        }
    }

    public void stopMediaPlayBack() {
        Intent localIntent = new Intent("com.android.music.musicservicecommand");
        localIntent.putExtra("command", "pause");
        sendBroadcast(localIntent);
    }

    public boolean isSDcardHasSpace() {
        File pathFile = android.os.Environment.getExternalStorageDirectory();
        StatFs statfs = new StatFs(pathFile.getPath());
        if (statfs.getAvailableBlocks() > 1) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (this.mRecorder.state()) {
            case RecorderUtils.IDLE_STATE:
                this.mRecorder.delete();
                break;
            case RecorderUtils.PLAYING_STATE:
                this.mRecorder.stop();
                this.mRecorder.delete();
                break;
        }
        mRecorder.stopPlayback();
        mRetestBtn.setEnabled(false);
        this.mHandler.sendEmptyMessage(MSG_TEST_MIC_START);
    }

    class MyHandler extends Handler {
        MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                case MSG_TEST_MIC_START:
                    removeMessages(MSG_TEST_MIC_START);
                    mTimes = RECORD_TIME;
                    mTitleUtile.setResult("  "+mTimes+" ");
                    mRecorder.startRecording(3, ".amr");
                    sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
                    break;
                case MSG_TEST_MIC_ING:
                    if (mTimes > 0) {
                        mTitleUtile.setResult("  "+mTimes+" ");
                        mTimes--;
                        LogUtils.LogD(TAG, "mTimes=" + mTimes);
                        sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
                    } else {
                        removeMessages(MSG_TEST_MIC_ING);
                        sendEmptyMessage(MSG_TEST_MIC_OVER);
                    }
                    break;
                case MSG_TEST_MIC_OVER:
                    removeMessages(MSG_TEST_MIC_OVER);
                    mRecorder.stopRecording();
                    if (mRecorder.sampleLength() > 0) {
                        mTitleUtile.setResult(R.string.HeadsetRecodrSuccess);
                        mRecorder.startPlayback();
                    } else {
                        mTitleUtile.setResult(R.string.RecordError);
                    }
                    mRetestBtn.setEnabled(true);
                    break;
            }
            mLevelMeterView.invalidate();
        }

    }
}
