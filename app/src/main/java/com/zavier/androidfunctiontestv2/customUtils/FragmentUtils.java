package com.zavier.androidfunctiontestv2.customUtils;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentUtils {
    private static final String TAG = "FragmentUtils";

    public static void startFragment(Context context, int funcType){
        //1,获取碎片管理器
        FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
        //2,碎片的显示需要使用FragmentTransaction类操作
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (funcType){
            case ParametersUtils.TEST_DISPLAY_COLOR:
                LogUtils.LogD(TAG, "fragment is TEST_DISPLAY_COLOR");
               // fragment = new DisplayFragment();
                break;
        }

        if(fragment != null){
            LogUtils.LogD(TAG, "fragment is null");
            transaction.replace(android.R.id.content, fragment);
            //使用FragmentTransaction必须要commit
            transaction.commit();
        }
    }
}
