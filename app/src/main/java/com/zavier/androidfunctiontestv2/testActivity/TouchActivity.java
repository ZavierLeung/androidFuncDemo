package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;
import com.zavier.androidfunctiontestv2.customView.CustomPaintView;

public class TouchActivity extends AppCompatActivity{

    private TextViewTitleUtils mTitleUtile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
        initView();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(TouchActivity.this);
        mTitleUtile.setTitle(R.string.main_func_touch);
        ((CustomPaintView)findViewById(R.id.touch_custom_view)).setToolType(MotionEvent.TOOL_TYPE_FINGER);
    }

}
