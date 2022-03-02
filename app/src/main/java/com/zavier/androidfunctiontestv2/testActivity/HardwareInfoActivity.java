package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.PermissionsUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardwareInfoActivity extends AppCompatActivity {
    private static final String TAG = "HardwareInfoActivity";
    private TextViewTitleUtils mTitleUtile;
    private TextView mTextView;
    private static final int ROM_FLAG = 0;
    private static final int RAM_FLAG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware_info);
        initView();
    }

    private void initView() {
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(HardwareInfoActivity.this);
        mTitleUtile.setTitle(R.string.main_func_hardware_info);
        mTextView = findViewById(R.id.device_info_tv);
        //getAll();
        getDevice();
        getDisplay();
        getDpi();
       // getRoot();
    }

    private String getStr(int id){
        return getResources().getString(id);
    }

    private void getRoot() {
        if (PermissionsUtils.checkGetRootAuth()) {
            mTextView.append(getStr(R.string.hardware_info_test_root) + "yes \n");
        } else {
            mTextView.append(getStr(R.string.hardware_info_test_root)+ "no \n");
        }
    }

    private void getDisplay() {
        WindowManager windowManager = getWindow().getWindowManager();
        Point point = new Point();
        windowManager.getDefaultDisplay().getRealSize(point);
        //屏幕实际宽度（像素个数）
        int width = point.x;
        //屏幕实际高度（像素个数）
        int height = point.y;
        mTextView.append(getStr(R.string.hardware_info_test_screen) + width + " * " + height + "\n");
    }

    private void getDpi() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int dens = dm.densityDpi;
        mTextView.append(getStr(R.string.hardware_info_test_dpi) + dens + "\n");
    }

    private void getDevice() {
        // 设备品牌
        String brand = Build.BRAND;
        // 设备型号
        String model = Build.MODEL;
        // 设备型号
        String version = Build.VERSION.RELEASE;
//        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                String deviceId = tm.getDeviceId();
//                String sim = tm.getSimSerialNumber();
//                //mTextView.append(getStr(R.string.hardware_info_test_device_id)+ deviceId + "\n");
//                //mTextView.append(getStr(R.string.hardware_info_test_sim)+ sim + "\n");
//            }
//        }
        // 设备版本号
        String version_num = Build.DISPLAY;
        // 设备CPU
        String cpu = Build.MANUFACTURER + " " + PermissionsUtils.getSystemProperty("ro.board.platform");
        //mTextView.append(getStr(R.string.hardware_info_test_brand)+ brand + "\n");
        mTextView.append(getStr(R.string.hardware_info_test_device_model)+ model + "\n");
        mTextView.append(getStr(R.string.hardware_info_test_version)+ version + "\n");
        mTextView.append(getStr(R.string.hardware_info_test_version_num)+ version_num + "\n");
        mTextView.append(getStr(R.string.hardware_info_test_cpu)+ cpu + "\n");

        if(getTotalMemorySize().equals("Unavailable")){
            mTextView.append(getStr(R.string.hardware_info_test_ram) + getRamSize() + "GB\n");
        } else {
            mTextView.append(getStr(R.string.hardware_info_test_ram) + getTotalMemorySize() + "GB\n");
        }

        if("Unavailable".equals(getEmmcSize())){
            mTextView.append(getStr(R.string.hardware_info_test_rom) + getRomSize() + "GB\n");
        } else {
            mTextView.append(getStr(R.string.hardware_info_test_rom) + getEmmcSize() + "GB\n");
        }

    }

    public String getTotalMemorySize() {
        try {
            FileReader fr = new FileReader("/proc/meminfo");
            BufferedReader br = new BufferedReader(fr, 2048);
//            String memoryLine = br.readLine();
            String subMemoryLine = "";
            String Line = "";
            while ((Line = br.readLine()) != null) {
                if (Line.contains("MemTotal:")){
                    subMemoryLine = Line.substring(Line.indexOf("MemTotal:"));
                    break;
                }
            }
            br.close();
            LogUtils.LogD(TAG,"TotalMemory: "+subMemoryLine);
            Matcher mer = Pattern.compile("^[0-9]+$").matcher(subMemoryLine.replaceAll("\\D+", ""));
            if (mer.find()) {
                long memSize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) ;
                LogUtils.LogD(TAG,"TotalMemory memSize: "+memSize);
                return String.valueOf(transformKBtoGB(RAM_FLAG, memSize));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unavailable";
    }

    private String getRamSize(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        LogUtils.LogD(TAG,"totalMem:" + info.totalMem);
        return String.valueOf(transformKBtoGB(RAM_FLAG, info.totalMem / 1024));
    }

    private String getRomSize(){
        //调用该类来获取磁盘信息（而getDataDirectory就是内部存储）
        final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long totalCounts = statFs.getBlockCountLong();//总共的block数
        long availableCounts = statFs.getAvailableBlocksLong() ; //获取可用的block数
        long size = statFs.getBlockSizeLong(); //每格所占的大小，一般是4KB==
        long availROMSize = availableCounts * size;//可用内部存储大小
        long totalROMSize = totalCounts *size; //内部存储总大小
        LogUtils.LogD(TAG,"totalCounts:" + totalCounts+" availableCounts:"+availableCounts+" size" +
                ":"+size);
        LogUtils.LogD(TAG,"totalROMSize:" + totalROMSize+" availROMSize:"+availROMSize);
        return String.valueOf(transformKBtoGB(ROM_FLAG, totalROMSize /1024));
    }

    //ROM大小
    public String getEmmcSize() {
        try {
            FileReader fr = new FileReader("/proc/partitions");
            BufferedReader br = new BufferedReader(fr, 2048);
            String Line = "";
            String EmmcSize = "";
            while ((Line = br.readLine()) != null)
            {
                if (Line.length() >7 && Line.lastIndexOf(" ") > 0) {
                    String str = Line.substring(Line.lastIndexOf(" ") + 1);
                    if (str.equals("mmcblk0"))
                    {
                        LogUtils.LogD(TAG,"EmmcSize Line: "+Line);
                        EmmcSize = Line.replaceAll(" ","");
                       // break;
                    }
                    LogUtils.LogD(TAG,"EmmcSize: "+EmmcSize);
                    if(EmmcSize.isEmpty()){
                        if (str.equals("mmcblk1"))
                        {
                            LogUtils.LogD(TAG,"EmmcSize Line: "+Line);
                            EmmcSize = Line.replaceAll(" ","");
                            break;
                        }
                    }
                    if(EmmcSize.isEmpty()){
                        if (str.equals("mmcblk2"))
                        {
                            LogUtils.LogD(TAG,"EmmcSize Line: "+Line);
                            EmmcSize = Line.replaceAll(" ","");
                            break;
                        }
                    }
                }

            }
            br.close();
            if (EmmcSize.length() >7 ){
                EmmcSize = (String) EmmcSize.subSequence(4, EmmcSize.length()-7);
                Matcher mer = Pattern.compile("^[0-9]+$").matcher(EmmcSize);
                if (mer.find()) {
                    long memSize = Integer.parseInt(EmmcSize);
                    LogUtils.LogD(TAG,
                            "EmmcSize EmmcSize: "+EmmcSize + " memSize:" + memSize);
                    return String.valueOf(transformKBtoGB(ROM_FLAG,memSize));
                }
            }
            return "0";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unavailable";
    }

    int transformKBtoGB(int flag, long valus){
        //flag: 1 is ram, 2 is rom
        int ret = 1;
        double tmp = 0;
        tmp = (double) valus / (1024 * 1024);
        if(tmp <= 0){
            return 0;
        }
        LogUtils.LogD(TAG," valus :"+ tmp);
        if(RAM_FLAG == flag){
            for(; ret*1.024 < tmp; ret++){
            }
        } else if(ROM_FLAG == flag){
            for(int i = 0; ret <= tmp; i++){
                ret = 1 << i;
            }
        }
        return  ret;
    }
}
