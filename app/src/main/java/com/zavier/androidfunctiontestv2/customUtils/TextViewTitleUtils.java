package com.zavier.androidfunctiontestv2.customUtils;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;

public class TextViewTitleUtils {
    private static final String TAG = "ControlButtonUtils";
    private Activity mActivity;
    private TextView mTitleTv, mSubTitleTv, mResultTv;

    public TextViewTitleUtils(Activity paramActivity) {
        mActivity = paramActivity;
        initView(false ,false);
    }

    public TextViewTitleUtils(Activity paramActivity, boolean subTitle, boolean resultTitle) {
        mActivity = paramActivity;
        initView(subTitle, resultTitle);
    }

    private void initView(boolean subTitle, boolean resultTitle){
        mTitleTv = mActivity.findViewById(R.id.main_func_title_tv);
        mSubTitleTv = mActivity.findViewById(R.id.main_func_sub_title_tv);
        mResultTv = mActivity.findViewById(R.id.main_func_result_tv);
        if(!subTitle){
            mSubTitleTv.setVisibility(View.GONE);
        }
        if(!resultTitle){
            mResultTv.setVisibility(View.GONE);
        }
    }

    public void setTitle(int id){
        mTitleTv.setText(id);
    }

    public void setSubTitle(int id){
        mSubTitleTv.setText(id);
    }

    public void setSubTitle(String id){
        mSubTitleTv.setText(id);
    }

    public void setResult(int id){
        mResultTv.setText(id);
    }

    public void setResult(String s){
        mResultTv.setText(s);
    }

    public void setTitleVisibility(int visibility){
        mTitleTv.setVisibility(visibility);
    }

    public void setSubTitleVisibility(int visibility){
        mSubTitleTv.setVisibility(visibility);
    }

    public void setResultVisibility(int visibility){
        mResultTv.setVisibility(visibility);
    }


    public void Hide() {
        mActivity.findViewById(R.id.main_func_title_tv).setVisibility(View.GONE);
        mActivity.findViewById(R.id.main_func_sub_title_tv).setVisibility(View.GONE);
        mActivity.findViewById(R.id.main_func_sub_title_tv).setVisibility(View.GONE);
    }

    public void Show() {
        mActivity.findViewById(R.id.main_func_title_tv).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.main_func_sub_title_tv).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.main_func_sub_title_tv).setVisibility(View.VISIBLE);
    }
}
