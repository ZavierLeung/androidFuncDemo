package com.zavier.androidfunctiontestv2.customUtils;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.R;

public class ControlButtonUtils implements View.OnClickListener {
    private static final String TAG = "ControlButtonUtils";
    private Activity mActivity;
    private Button mReturnBtn, mPassBtn, mNoPassBtn;
    private int mFuncType;

    public ControlButtonUtils(Activity paramActivity, int type) {
        mActivity = paramActivity;
        mFuncType = type;
        initView();
    }

    private void initView(){
        mReturnBtn = mActivity.findViewById(R.id.test_return_btn);
        mPassBtn = mActivity.findViewById(R.id.test_pass_btn);
        mNoPassBtn = mActivity.findViewById(R.id.test_no_pass_btn);

        mReturnBtn.setOnClickListener(this);
        mPassBtn.setOnClickListener(this);
        mNoPassBtn.setOnClickListener(this);

        if(MainApplication.getTestAll()){
            mReturnBtn.setVisibility(View.INVISIBLE);
        } else{
            mReturnBtn.setVisibility(View.VISIBLE);
        }
    }

    public void hideReturnBtn(int ret){
        mReturnBtn.setVisibility(ret);
    }

    public void Hide() {
        mActivity.findViewById(R.id.test_pass_btn).setVisibility(View.INVISIBLE);
        mActivity.findViewById(R.id.test_no_pass_btn).setVisibility(View.INVISIBLE);
        mActivity.findViewById(R.id.test_return_btn).setVisibility(View.INVISIBLE);
    }

    public void Show() {
        mActivity.findViewById(R.id.test_pass_btn).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.test_pass_btn).requestFocus();
        mActivity.findViewById(R.id.test_no_pass_btn).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.test_no_pass_btn).requestFocus();
        if(!MainApplication.getTestAll()){
            mActivity.findViewById(R.id.test_return_btn).setVisibility(View.VISIBLE);
            mActivity.findViewById(R.id.test_return_btn).requestFocus();
        }
    }


    @Override
    public void onClick(View view) {
        if(mFuncType == -1){
            LogUtils.LogD(TAG, " mFuncType is -1");
            return ;
        }
        switch (view.getId()){
            case R.id.test_pass_btn:
                LogUtils.LogD(TAG, " mFuncType:"+mFuncType);
                ActivityUtils.returnOrNextActivity(mActivity, mFuncType, true);
                break;
            case R.id.test_no_pass_btn:
                ActivityUtils.returnOrNextActivity(mActivity, mFuncType, false);
                break;
            case R.id.test_return_btn:
                ActivityUtils.returnToMainActivity(mActivity);
                break;
        }
    }
}
