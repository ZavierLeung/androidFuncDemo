package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.ReceiverUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

public class UsbExternalActivity extends AppCompatActivity {
    private TextView mTextView;
    private TextViewTitleUtils mTitleUtile;
    private ReceiverUtils mReceiverUtils;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_external);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this,true,false);
        mTitleUtile.setTitle(R.string.main_func_usb_external);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
