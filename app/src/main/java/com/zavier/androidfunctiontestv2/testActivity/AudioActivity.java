package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mPlayBtn;
    private SoundPool mSoundPool;
    private int mSound;
    private int mStreamID = -1;
    private TextViewTitleUtils mTitleUtile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initView();
        playSoundPool();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(AudioActivity.this);
        mTitleUtile.setTitle(R.string.main_func_audio);

        mPlayBtn = findViewById(R.id.audio_test_play_btn);
        mPlayBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.audio_test_play_btn:
                start();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    private void playSoundPool(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = null;
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else { // 5.0 以前
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        }
        mSound = mSoundPool.load(this, R.raw.sample, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                start();
            }
        });
    }

    public void start(){
        if (mSoundPool != null) {
            mStreamID = mSoundPool.play(mSound, 1, 1, 16, 0, 1.0f);
        }

    }

    public void pause() {
        if (mSoundPool != null) {
            mSoundPool.pause(mStreamID);
        }
    }
}
