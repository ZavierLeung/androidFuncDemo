package com.zavier.androidfunctiontestv2.customUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.R;

public class ExitButtonUtils implements View.OnClickListener {
    private static final String TAG = "ExitButtonUtils";
    private Activity mActivity;
    private Button mExitBtn, mUninstallBtn;

    public ExitButtonUtils(Activity paramActivity) {
        mActivity = paramActivity;
        initView();
    }

    private void initView(){
        mExitBtn = mActivity.findViewById(R.id.main_func_test_exit_btn);
        mUninstallBtn = mActivity.findViewById(R.id.main_func_test_uninstall_btn);

        mExitBtn.setOnClickListener(this);
        mUninstallBtn.setOnClickListener(this);
    }

    public void Hide() {
        mActivity.findViewById(R.id.main_func_test_exit_btn).setVisibility(View.GONE);
        mActivity.findViewById(R.id.main_func_test_uninstall_btn).setVisibility(View.GONE);
    }

    public void Show() {
        mActivity.findViewById(R.id.main_func_test_exit_btn).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.main_func_test_exit_btn).requestFocus();
        mActivity.findViewById(R.id.main_func_test_uninstall_btn).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.main_func_test_uninstall_btn).requestFocus();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_func_test_exit_btn:
                MainApplication.setTestAll(false);
                exit();
                break;
            case R.id.main_func_test_uninstall_btn:
                DialogUtils dialog = new DialogUtils(mActivity);
                String content = mActivity.getResources().getString(R.string.dialog_content_string);
                dialog.setMessage(content).setConfirm(new DialogUtils.IOnConfirmListener() {
                    @Override
                    public void onConfirm(DialogUtils dialog) {
                        PermissionsUtils.setSystemProperty("persist.sys.copy_file","1");
                        dialog.dismiss();
                    }
                }).setCancel(new DialogUtils.IOnCancelListener() {
                    @Override
                    public void onCancel(DialogUtils dialog) {
                        dialog.dismiss();
                    }
                }).show();
                break;

        }
    }

    public static void exit(){
        //先让app进入后台
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getContext().startActivity(intent);

        //调用系统API结束进程
        android.os.Process.killProcess(android.os.Process.myPid());

        //结束整个虚拟机进程，注意如果在manifest里用android:process给app指定了不止一个进程，则只会结束当前进程
        System.exit(0);
    }
}
