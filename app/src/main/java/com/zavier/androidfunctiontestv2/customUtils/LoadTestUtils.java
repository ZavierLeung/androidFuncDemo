package com.zavier.androidfunctiontestv2.customUtils;

import android.content.Context;
import android.hardware.Sensor;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customAdapter.RecyclerViewAdapter;

import java.util.List;

public class LoadTestUtils {

    public static void loadMainFunc(Context context, RecyclerViewAdapter.RecyclerViewHolder viewHolder, int pos,
                                    int funcFlag) {
        viewHolder.setMainFunTv(ParametersUtils.funcNameIdList.get(pos));
        viewHolder.setImageView(funcFlag);
    }

    public static void loadSensor(Context context, RecyclerViewAdapter.RecyclerViewHolder viewHolder, int pos,
                                  List<Sensor> list) {
        if(list != null){
            viewHolder.setSensorName(SensorUtils.getSensorName(list.get(pos)));
        }
    }
}
