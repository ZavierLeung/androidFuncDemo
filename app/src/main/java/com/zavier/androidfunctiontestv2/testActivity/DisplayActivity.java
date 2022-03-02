package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

public class DisplayActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mPassBtn, mNoPassbtn;
    private TextView mTv;
    private LinearLayout mLayout;
    private int mCount = 0;
    private ControlButtonUtils controlButtonUtils;
    private TextViewTitleUtils mTitleUtile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        initView();
    }

    private void initView(){
        controlButtonUtils = new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(DisplayActivity.this);
        mTitleUtile.setTitle(R.string.main_func_display);
        mLayout = findViewById(R.id.display_layout);
        mTv = findViewById(R.id.display_tv);
        setBtnShow(true);
        mLayout.setOnClickListener(this);

    }

    private void setBtnShow(boolean isSown){
        if(isSown){
            controlButtonUtils.Show();
            mTv.setVisibility(View.VISIBLE);
        } else {
            controlButtonUtils.Hide();
            mTv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.display_layout:
                mTitleUtile.setTitleVisibility(View.INVISIBLE);
                changeColor();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void changeColor(){
        setBtnShow(false);
        mCount = mCount % 10;
        switch (mCount){
            case 0:
                mLayout.setBackgroundResource(R.color.colorWhite); break;
            case 1:
                mLayout.setBackgroundResource(R.color.colorRed); break;
            case 2:
                mLayout.setBackgroundResource(R.color.colorGreen); break;
            case 3:
                mLayout.setBackgroundResource(R.color.colorBlue); break;
            case 4:
                mLayout.setBackgroundResource(R.drawable.gray150); break;
            case 5:
                mLayout.setBackgroundResource(R.drawable.gray127); break;
            case 6:
                mLayout.setBackgroundResource(R.drawable.gray63); break;
            case 7:
                mLayout.setBackgroundResource(R.drawable.colorbar08); break;
            case 8:
                mLayout.setBackgroundResource(R.color.colorBack); break;
            case 9:
                mLayout.setBackgroundResource(R.color.colorWhite);
                setBtnShow(true);
                mTv.setText(R.string.display_test_success);
                mLayout.setClickable(false);
                mTitleUtile.setTitleVisibility(View.VISIBLE);
                break;
        }
        mCount++;
    }
}
