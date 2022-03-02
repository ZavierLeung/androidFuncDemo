package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";
    private VideoView mPlayerView;
    private TextViewTitleUtils mTitleUtile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        initPlayer();
    }


    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this);
        mTitleUtile.setTitle(R.string.main_func_video);

        mPlayerView = findViewById(R.id.video_test_videoview);
        mPlayerView.setMediaController(new MediaController(this));
        //监听视频播放完的代码
        mPlayerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                // TODO Auto-generated method stub
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
    }

    private void initPlayer() {
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/raw/video");
        LogUtils.LogD(TAG,"video path: "+uri);
        mPlayerView.setVideoURI(uri);
        mPlayerView.start();
    }
}
