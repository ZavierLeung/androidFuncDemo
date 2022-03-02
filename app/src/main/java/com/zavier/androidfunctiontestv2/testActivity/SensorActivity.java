package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.MainFunctionActivity;
import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customAdapter.RecyclerViewAdapter;
import com.zavier.androidfunctiontestv2.customUtils.ActivityUtils;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SensorActivity extends AppCompatActivity {
    private static final String TAG = "SensorActivity";
    private TextViewTitleUtils mTitleUtile;
    private RecyclerView mRecyclerView;
    private int mItemcount = 0;
    private SensorManager mSensorManager;
    private List<Sensor> mSensorList;
    private List<SensorListener> mSensorListener = new ArrayList<SensorListener>();

    private final int SENSOR_1 = 1;
    private final int SENSOR_2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSensor();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this, true,false);
        mTitleUtile.setTitle(R.string.main_func_sensor);
        mTitleUtile.setSubTitle(R.string.sensor_test_sub_title);
        initSensorList();
        if(mSensorList.size() > 0){
            mRecyclerView = findViewById(R.id.sensor_test_recview);
            mItemcount = mSensorList.size();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(new RecyclerViewAdapter(this, mSensorList, R.layout.recycler_sensor,
                    mItemcount, ParametersUtils.TYPE_SENSOR_TEST, new RecyclerViewAdapter.OnItemCheckListener() {
                        @Override
                        public void onCheck(View view, int pos, Sensor sensor,
                                            RecyclerViewAdapter.RecyclerViewHolder viewHolder) {
                            if(viewHolder.mSensorCb.isChecked()){
                                unregisterSensor();
                                registerSensor(viewHolder, sensor, getSameSensorNum(sensor.getType()));
                            } else {
                                viewHolder.showSensorLLayout(false,false);
                                unregisterSensor();
                            }
                        }
            }));
        }
    }

    private void initSensorList(){
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    private void unregisterSensor(){
        if(mSensorManager == null){
            return ;
        }

        for (int i = 0; i < mSensorListener.size(); i++){
            mSensorManager.unregisterListener(mSensorListener.get(i));
        }
        mSensorListener.clear();
    }

    private void registerSensor(RecyclerViewAdapter.RecyclerViewHolder viewHolder, Sensor sensor, int num){
        if(mSensorManager == null){
            return ;
        }
        LogUtils.LogD(TAG, "registerSensor getName:"+sensor.getName() + " type:"+ sensor.getType());
        if(num == 1){
            viewHolder.showSensorLLayout(true,false);
            mSensorListener.add(new SensorListener(viewHolder));
            mSensorManager.registerListener(mSensorListener.get(0), sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if(num == 2){
            viewHolder.showSensorLLayout(true,true);
            mSensorListener.add(new SensorListener(viewHolder));
            mSensorListener.add(new SensorListener(viewHolder, SENSOR_2));
            mSensorManager.registerListener(mSensorListener.get(0), sensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mSensorListener.get(1), sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private int getSameSensorNum(int type){
        int num = 0;
        for(Sensor sensor : mSensorList){
            if(sensor.getType() == type){
                num++;
            }
        }
        return num;
    }

    private class SensorListener implements SensorEventListener {

        private RecyclerViewAdapter.RecyclerViewHolder mViewHolder;
        private int mSensorNum = SENSOR_1;

        private SensorListener(RecyclerViewAdapter.RecyclerViewHolder viewHolder){
            mViewHolder = viewHolder;
        }

        private SensorListener(RecyclerViewAdapter.RecyclerViewHolder viewHolder, int sensorNum){
            mViewHolder = viewHolder;
            mSensorNum = sensorNum;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            DecimalFormat decimalFormat=new DecimalFormat("0.0000");
            try {
                String x = decimalFormat.format(sensorEvent.values[0]);
                String y = decimalFormat.format(sensorEvent.values[1]);
                String z = decimalFormat.format(sensorEvent.values[2]);
                if(mSensorNum == SENSOR_1){
                    mViewHolder.setSensor1XYZ(x, y, z);
                } else if(mSensorNum == SENSOR_2){
                    mViewHolder.setSensor2XYZ(x, y, z);
                }
            } catch (Exception e){

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }


}
