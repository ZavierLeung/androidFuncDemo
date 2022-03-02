package com.zavier.androidfunctiontestv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.zavier.androidfunctiontestv2.customAdapter.RecyclerViewAdapter;
import com.zavier.androidfunctiontestv2.customSql.SqlConstant;
import com.zavier.androidfunctiontestv2.customSql.SqlHelper;
import com.zavier.androidfunctiontestv2.customSql.SqlStaticData;
import com.zavier.androidfunctiontestv2.customUtils.ActivityUtils;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.ExitButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.PermissionsUtils;

import java.util.ArrayList;

public class MainFunctionActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainFunctionActivity";
    private RecyclerView mRecyclerView;
    private int mItemcount = ParametersUtils.TEST_MAX;
    private int mWidth;
    private int mHeight;
    private static SqlHelper mSqlHelper;
    private Button mRestartBtn;
    private PermissionsUtils mPermissionsUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        setContentView(R.layout.activity_main);
        if(!PermissionsUtils.getAppInfo()){
            LogUtils.LogI(TAG,"onCreate getAPP");
            //ExitButtonUtils.exit();
            //return;
        }
        initDBHelper();
        mPermissionsUtils = new PermissionsUtils(this);
        new ExitButtonUtils(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        LogUtils.LogD(TAG, "onResume width: " + mWidth + " height: " + mHeight);
        if(mWidth >= 1280){
            initView(3);
        } else if(mWidth >= 800){
            initView(2);
        } else {
            initView(1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.closeDBHelper();
        MainApplication.setTestAll(false);
    }

    private void initDBHelper(){
        mSqlHelper = MainApplication.getSqlHelper();
        createDBData();
    }

    private void createDBData(){
        if(mSqlHelper != null){
            boolean ret = mSqlHelper.queryByNameIsExist(ParametersUtils.funcNameList.get(0));
            if(!ret){
                resetDBData();
            }
        }
    }

    private void resetDBData(){
        ArrayList<SqlStaticData> info = new ArrayList<SqlStaticData>();
        for(int i = 0; i < ParametersUtils.funcNameList.size(); i++){
            SqlStaticData info1 = new SqlStaticData();
            info1.rowId = i;
            info1.funcName = ParametersUtils.funcNameList.get(i);
            info1.funcStatus = MessageUtils.FLAG_TEST_READY;
            info.add(info1);
        }
        LogUtils.LogD(TAG, "SqlStaticData list ： " + info.toString());
        if(mSqlHelper != null) {
            long ret1 = mSqlHelper.insertMore(info);
            LogUtils.LogD(TAG, "createDBData ret ： " + ret1);
        } else {
            LogUtils.LogI(TAG, "mSqlHelper is null");
        }
    }

    private void updateDBData(int pos){
        if(mSqlHelper != null) {
            SqlStaticData info1 = new SqlStaticData();
            info1.rowId = pos + 1;
            info1.funcName = ParametersUtils.funcNameList.get(pos);
            info1.funcStatus = MessageUtils.FLAG_TEST_SUCCESS;
            int ret = mSqlHelper.update(info1);
            LogUtils.LogD(TAG, "updateDBData ret : "+ret);
        }
    }

    private void initView(int spanCount){

        mRestartBtn = findViewById(R.id.main_func_restart_btn);
        mRecyclerView = findViewById(R.id.main_function_recview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(MainFunctionActivity.this, spanCount));
        mRecyclerView.setAdapter(new RecyclerViewAdapter(MainFunctionActivity.this,
                R.layout.recycler_main_func, mItemcount, ParametersUtils.TYPE_MAIN_FUNC, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int pos) {
                //updateDBData(pos);
                ActivityUtils.startFuncActivity(MainFunctionActivity.this, pos);
            }
        }));
        mRestartBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_func_restart_btn:
                MainApplication.setTestAll(true);
                ActivityUtils.startFuncActivity(MainFunctionActivity.this, ParametersUtils.TEST_DISPLAY_COLOR);
                break;
        }
    }
}
