package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.FileUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.NetworkUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.PermissionsUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NetworkActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, NetworkUtils.OnNetworkStatusChangedListener {
    private static final String TAG = "NetworkActivity";
    private TextViewTitleUtils mTitleUtile;
    private CheckBox mSaveFileCB,mStartCB;
    private TextView mNetworkStatusTV, mNetworkPingTV;
    private NetworkUtils mNetworkUtils;
    private static int mDisconnectNum = 0;
    private static int mLossPackageNum = 0;
    private boolean mFirstEnter = true;
    private boolean mThreadStart = false;
    private int mNetType = 0;
    private final static int TV_NULL = 1;
    private final static int TV_STATUS = 2;
    private final static int TV_PING = 3;

    private final static int CLEAR_TV = 1;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TV_NULL:
                    mNetworkPingTV.setText("");
                    break;
                case TV_STATUS:
                    if(mNetworkStatusTV != null){
                        if(msg.arg2 == CLEAR_TV){
                            mNetworkStatusTV.setText("");
                        }
                        mNetworkStatusTV.append(getNetworkStatus(msg.arg1));
                        if(mSaveFileCB.isChecked())
                            FileUtils.writeFileOfAppend(LogUtils.NETWORK_STATUS_FILE, getNetworkStatus(msg.arg1));
                    }
                    break;
                case TV_PING:
                    if(mNetworkPingTV != null){
                        mNetworkPingTV.append(msg.obj.toString());
                        if(mSaveFileCB.isChecked())
                            FileUtils.writeFileOfAppend(LogUtils.NETWORK_PING_FILE,msg.obj.toString());
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        initView();
        initUtils();
        runNetworkStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mNetworkUtils != null){
            mNetworkUtils.unregisterNetworkStatusChangedListener(this);
        }
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this,true,false);
        mTitleUtile.setTitle(R.string.main_func_network);
        mTitleUtile.setSubTitle(R.string.network_test_sub);
        mSaveFileCB = findViewById(R.id.network_test_save_file_cbox);
        mSaveFileCB.setText(getResources().getString(R.string.network_test_save_file) + " ( " + LogUtils.NETWORK_FILE_PATH + " )");
        mNetworkStatusTV = findViewById(R.id.network_test_status_tv);
        mNetworkPingTV = findViewById(R.id.network_test_ping_tv);
        mStartCB = findViewById(R.id.network_test_start_cb);
        mStartCB.setOnCheckedChangeListener(this);

    }

    private void initUtils(){
        mNetworkUtils = new NetworkUtils(NetworkActivity.this);
        mNetworkUtils.registerNetworkStatusChangedListener(this);
    }

    private String networkStatusString(int status){
        String getCurTime = LogUtils.getTimeDateFormat();
        switch (status){
            case NetworkUtils.NETWORK_2G:
                return getCurTime + " Connecting data with 2G mobile";
            case NetworkUtils.NETWORK_3G:
                return getCurTime + " Connecting data with 3G mobile";
            case NetworkUtils.NETWORK_4G:
                return getCurTime + " Connecting data with 4G mobile";
            case NetworkUtils.NETWORK_WIFI:
                return getCurTime + " Connecting data with wifi";
            case NetworkUtils.NETWORK_ETHERNET:
                return getCurTime + " Connecting data with ethernet";
            case NetworkUtils.NETWORK_NO:
                LogUtils.LogD(TAG,"networkStatusString mDisconnectNum++");
                return getCurTime + " No network connection";
            default:
                return getCurTime + " Unknown network";
        }
    }

    public String getNetworkStatus(int status){
        return networkStatusString(status) + " (disconnect num : " +
                mDisconnectNum + " , more than 75% packet loss num:"+mLossPackageNum + ")\n";
    }

    private int getNetworkDate(){
        return mNetworkUtils.getNetworkType();
    }

    private void runNetworkStatus(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int num = 0;
                while (true){
                    if(mThreadStart){
                        try {
                            if(num >= 10){
                                Message msg = Message.obtain();
                                msg.what = TV_NULL;
                                handler.sendMessage(msg);
                                num = 0;
                            }
                            num++;
                            Message msg = Message.obtain();
                            msg.what = TV_PING;
                            msg.obj = getNetworkPing();
                            handler.sendMessage(msg);
                            Thread.sleep(8000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public String getNetworkPing() {
        Runtime runtime = Runtime.getRuntime();
        Process ipProcess = null;
        String getCurTime = LogUtils.getTimeDateFormat();
        try {
            ipProcess = runtime.exec("ping -c 4 -w 10 www.baidu.com");
            InputStream input = ipProcess.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content + "\n");
            }

            int exitValue = ipProcess.waitFor();
            LogUtils.LogD(TAG,"getNetworkPing exitValue: "+exitValue+" content: "+content+ " stringBuffer:"+stringBuffer);
            if (exitValue == 0) {
                //连接，网络正常
                return "==============\n" + getCurTime + " " +stringBuffer.toString();
            } else {
                if (stringBuffer.indexOf("100% packet loss") != -1 || stringBuffer.indexOf("95% packet loss") != -1 ||
                        stringBuffer.indexOf("90% packet loss") != -1 ||stringBuffer.indexOf("85% packet loss") != -1 ||
                        stringBuffer.indexOf("80% packet loss") != -1 || stringBuffer.indexOf("75% packet loss") != -1){
                    mLossPackageNum++;
                    Message message = Message.obtain();
                    message.arg1 = mNetType;
                    message.what = TV_STATUS;
                    handler.sendMessage(message);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (ipProcess != null) {
                ipProcess.destroy();
            }
            runtime.gc();
        }
        LogUtils.LogD(TAG,"getNetworkPing fail");
        return getCurTime + " ping www.baidu.com failed\n";
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton == mStartCB){
            if(mSaveFileCB.isChecked()){
                if(b && mFirstEnter){
                    FileUtils.createFileByDeleteOldFile(LogUtils.NETWORK_STATUS_FILE);
                    FileUtils.createFileByDeleteOldFile(LogUtils.NETWORK_PING_FILE);
                    mFirstEnter = false;
                }
            }
            if(b){
                mStartCB.setText(R.string.network_test_stop);
                mThreadStart = true;
                Message message = Message.obtain();
                message.arg1 = getNetworkDate();
                message.what = TV_STATUS;
                message.arg2 = CLEAR_TV;
                handler.sendMessage(message);
            } else {
                mStartCB.setText(R.string.network_test_start);
                mThreadStart = false;
            }
            mSaveFileCB.setClickable(!b);
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnected(int networkType) {
        this.mNetType = networkType;
        if(networkType == NetworkUtils.NETWORK_NO){
            mDisconnectNum++;
        }
        Message message = Message.obtain();
        message.arg1 = networkType;
        message.arg2 = CLEAR_TV;
        message.what = TV_STATUS;
        handler.sendMessage(message);

    }
}
