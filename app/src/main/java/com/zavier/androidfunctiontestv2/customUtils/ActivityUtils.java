package com.zavier.androidfunctiontestv2.customUtils;

import android.content.Context;
import android.content.Intent;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.MainFunctionActivity;
import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customSql.SqlStaticData;
import com.zavier.androidfunctiontestv2.testActivity.EndTestActivity;


import androidx.appcompat.app.AppCompatActivity;

public class ActivityUtils {

    private static final String TAG =  "ActivityUtils";

    public static void startFuncActivity(Context context, int pos){
        Intent intent = null;
        LogUtils.LogD(TAG, "startFuncActivity pos : "+pos);
        intent = new Intent(context, ParametersUtils.funcClassList.get(pos));
        if(intent != null){
            context.startActivity(intent);
//            ((AppCompatActivity)context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    public static void nextToFuncActivity(Context context, int pos){
        Intent intent = null;
        LogUtils.LogD(TAG, "nextToFuncActivity pos:"+pos +" size:"+ParametersUtils.funcClassList.size());
        if(pos == ParametersUtils.funcClassList.size() - 1){
            intent = new Intent(context, EndTestActivity.class);
        } else {
            intent = new Intent(context, ParametersUtils.funcClassList.get(pos+1));
        }

        if(intent != null){
            context.startActivity(intent);
//            ((AppCompatActivity)context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    public static void returnToMainActivity(Context context){
        Intent intent = new Intent(context, MainFunctionActivity.class);
        context.startActivity(intent);
        ((AppCompatActivity)context).finish();
//        ((AppCompatActivity)context).overridePendingTransition(R.anim.out_to_left, R.anim.in_from_right);
    }

    public static void returnOrNextActivity(Context context, int id, boolean testFlag){
        updataFuncStatus(context,id,testFlag);
        if(MainApplication.getTestAll()){
            nextToFuncActivity(context,id);
        } else {
            ((AppCompatActivity)context).finish();
//            ((AppCompatActivity)context).overridePendingTransition(R.anim.out_to_left, R.anim.in_from_right);
        }


    }

    private static void updataFuncStatus(Context context, int id, boolean testFlag){
        SqlStaticData info = new SqlStaticData();
        info.rowId = id + 1;
        info.funcName = ParametersUtils.funcNameList.get(id);
        if(testFlag){
            info.funcStatus = MessageUtils.FLAG_TEST_SUCCESS;
        } else {
            info.funcStatus = MessageUtils.FLAG_TEST_FAIL;
        }
        int ret = MainApplication.getSqlHelper().update(info);
        LogUtils.LogD(TAG, "returnActivity ret : "+ret);
    }

}
