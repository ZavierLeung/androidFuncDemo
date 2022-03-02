package com.zavier.androidfunctiontestv2;

import android.app.Application;
import android.content.Context;

import com.zavier.androidfunctiontestv2.customSql.SqlConstant;
import com.zavier.androidfunctiontestv2.customSql.SqlHelper;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;

public class MainApplication extends Application {
    private static Context mContext;
    private static SqlHelper mSqlHelper;
    private static boolean mTestAll = false;
    private static final String TAG = "MainApplication";
    public MainApplication() {
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
        initDBHelper();
    }

    private void initDBHelper(){
        LogUtils.LogD(TAG, "initDBHelper");
        mSqlHelper = SqlHelper.getInstance(mContext, SqlConstant.DB_VERSION);
        mSqlHelper.openWriteLink();
    }

    public static SqlHelper getSqlHelper(){
        LogUtils.LogD(TAG, "getSqlHelper");
        if(mSqlHelper == null){
            LogUtils.LogD(TAG, "getSqlHelper is null");
            mSqlHelper = SqlHelper.getInstance(mContext, SqlConstant.DB_VERSION);
            mSqlHelper.openWriteLink();
        }
        return mSqlHelper;
    }

    public static void closeDBHelper(){
        if(mSqlHelper != null){
            LogUtils.LogD(TAG, "closeDBHelper");
            mSqlHelper.closeDBLink();
            mSqlHelper = null;
        }
    }

    public static void setTestAll(boolean b){
        mTestAll = b;
    }

    public static boolean getTestAll(){
        return mTestAll;
    }

}
