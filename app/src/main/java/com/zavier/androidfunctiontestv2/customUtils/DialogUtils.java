package com.zavier.androidfunctiontestv2.customUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.zavier.androidfunctiontestv2.R;
import androidx.annotation.NonNull;


public class DialogUtils extends Dialog  implements View.OnClickListener{

    private TextView mTvTitle,mTvMessage,mTvCancel,mTvConfirm;
    private String mTitle,mMessage,mCancel,mConfirm;
    private IOnCancelListener cancelListener;
    private IOnConfirmListener confirmListener;
    private Context mContext;

    public DialogUtils(@NonNull Context context) {
        super(context);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public DialogUtils setTitle(String Title) {
        this.mTitle = Title;
        return this;
    }

    public DialogUtils setMessage(String Message) {
        this.mMessage = Message;
        return this;
    }

    public DialogUtils setCancel(String Cancel, IOnCancelListener listener) {
        this.mCancel = Cancel;
        this.cancelListener = listener;
        return this;
    }

    public DialogUtils setCancel(IOnCancelListener listener) {
        this.cancelListener = listener;
        return this;
    }

    public DialogUtils setConfirm(String Confirm, IOnConfirmListener listener) {
        this.mConfirm = Confirm;
        this.confirmListener = listener;
        this.setCancelable(false);
        return this;
    }

    public DialogUtils setConfirm(IOnConfirmListener listener) {
        this.confirmListener = listener;
        this.setCancelable(false);
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_custom_dialog);
        //设置弹框的宽度为当前手机屏幕的50%
        WindowManager manager = getWindow().getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        Point size = new Point();
        display.getSize(size);
        params.width = (int)(size.x * 0.5);
        getWindow().setAttributes(params);
        mTvTitle = findViewById(R.id.my_custom_dialog_tv1);
        mTvMessage = findViewById(R.id.my_custom_dialog_tv2);
        mTvCancel = findViewById(R.id.my_custom_dialog_tv3);
        mTvConfirm = findViewById(R.id.my_custom_dialog_tv4);
        setTextString();
        mTvCancel.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);
    }

    private void setTextString(){
        if(!TextUtils.isEmpty(mTitle)){
            mTvTitle.setText(mTitle);
        }
        if(!TextUtils.isEmpty(mMessage)){
            mTvMessage.setText(mMessage);
        }
        if(!TextUtils.isEmpty(mCancel)){
            mTvCancel.setText(mCancel);
        }
        if(!TextUtils.isEmpty(mConfirm)){
            mTvConfirm.setText(mConfirm);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.my_custom_dialog_tv3:
                if(cancelListener != null){
                    cancelListener.onCancel(this);
                }
                dismiss();
                break;
            case R.id.my_custom_dialog_tv4:
                if(confirmListener != null){
                    confirmListener.onConfirm(this);
                }
                dismiss();
                break;
        }
    }

    public interface IOnCancelListener{
        void onCancel(DialogUtils dialog);
    }

    public  interface  IOnConfirmListener{
        void onConfirm(DialogUtils dialog);
    }
}
