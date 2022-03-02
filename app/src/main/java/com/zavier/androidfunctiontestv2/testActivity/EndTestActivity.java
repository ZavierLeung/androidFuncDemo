package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customAdapter.RecyclerViewAdapter;
import com.zavier.androidfunctiontestv2.customUtils.ExitButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

public class EndTestActivity extends AppCompatActivity {
    private static final String TAG = "EndTestActivity";
    private TextViewTitleUtils mTitleUtile;
    private int mItemcount = ParametersUtils.TEST_MAX;
    private RecyclerView mRecyclerView;
    private int mWidth;
    private int mHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_test);
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

    private void initView(int spanCount){
        new ExitButtonUtils(this);
        mTitleUtile = new TextViewTitleUtils(EndTestActivity.this);
        mTitleUtile.setTitle(R.string.main_func_test_finish);
        mRecyclerView = findViewById(R.id.end_test_recview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(EndTestActivity.this, spanCount));
        mRecyclerView.setAdapter(new RecyclerViewAdapter(EndTestActivity.this, R.layout.recycler_main_func,
                ParametersUtils.TEST_MAX, ParametersUtils.TYPE_MAIN_FUNC, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int pos) {

            }
        }));
    }
}
