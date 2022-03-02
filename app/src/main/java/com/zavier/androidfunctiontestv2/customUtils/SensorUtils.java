package com.zavier.androidfunctiontestv2.customUtils;

import android.hardware.Sensor;

public class SensorUtils {

    public static String getSensorName(Sensor sensor){
        String name = null;
        String discription = null;
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                discription = "加速度传感器";
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                discription="外界温度传感器";
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                discription="转动向量传感器";
                break;
            case Sensor.TYPE_GRAVITY:
                discription="重力传感器";
                break;
            case Sensor.TYPE_GYROSCOPE:
                discription="陀螺仪";
                break;
            case Sensor.TYPE_LIGHT:
                discription="光照传感器";
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                discription="线性加速度传感器";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                discription="磁场传感器";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                discription="未校准磁场传感器";
                break;
            case Sensor.TYPE_PROXIMITY:
                discription="近距离传感器";
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                discription="旋转向量传感器";
                break;
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                discription="有力动作感应器";
                break;
            case Sensor.TYPE_TEMPERATURE:
                discription="cpu温度感应器";
                break;
            case Sensor.TYPE_PRESSURE:
                discription="压力感应器";
                break;
            case Sensor.TYPE_STEP_COUNTER:
                discription="计步器";
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                discription="相对湿度感应器";
                break;
            case Sensor.TYPE_ORIENTATION:
                discription="方向感应器";
                break;
            default:
                break;
        }
        if(discription == null){
            name = sensor.getName();
        } else {
            name = sensor.getName() + "(" + discription + ")";
        }
        return name;
    }
}
