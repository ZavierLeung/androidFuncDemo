package com.zavier.androidfunctiontestv2.testActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.ReceiverUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WifiActivity extends AppCompatActivity {
    private static final String TAG = "WifiActivity";
    private TextViewTitleUtils mTitleUtile;
    private TextView mInfoTv, mResultTv;
    private ReceiverUtils mReceiverUtils;
    private WifiManager mWifiManager;
    private List<String> mWifiList = new ArrayList<String>();
    private boolean mReadyToTest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openWifi();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mWifiManager.isWifiEnabled()){
                    mInfoTv.setText(R.string.wifi_test_open_success);
                    mInfoTv.append("\n");
                    mInfoTv.append(getResources().getString(R.string.wifi_test_scan));
                    wifiScan();
                    initReceiver();
                } else {
                    mInfoTv.setText(R.string.wifi_test_open_fail);
                    mInfoTv.append("\n");
                    mInfoTv.append(getResources().getString(R.string.wifi_test_reopen));
                    reopenWifi();
                }
            }
        },2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeReceiver();
        closeWifi();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this);
        mTitleUtile.setTitle(R.string.main_func_wifi);
        mInfoTv = findViewById(R.id.wifi_test_info_tv);
        mResultTv = findViewById(R.id.wifi_test_result_tv);
        mInfoTv.setText(R.string.wifi_test_open);
    }

    private void initReceiver(){
        mReceiverUtils = new ReceiverUtils(mHandler,this.getClass());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiverUtils, intentFilter);
    }

    private void closeReceiver(){
        if(mReceiverUtils != null){
            unregisterReceiver(mReceiverUtils);
        }
    }

    private void openWifi(){
        mWifiList.clear();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);
    }

    private void reopenWifi(){
        mWifiManager.setWifiEnabled(false);
        mWifiList.clear();
        mWifiManager.setWifiEnabled(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mWifiManager.isWifiEnabled()){
                    mInfoTv.setText(R.string.wifi_test_open_success);
                    mInfoTv.append("\n");
                    mInfoTv.append(getResources().getString(R.string.wifi_test_scan));
                    wifiScan();
                    initReceiver();
                } else {
                    mInfoTv.setText(R.string.wifi_test_open_fail);
                }
            }
        },5000);
    }

    private void closeWifi(){
        if(mWifiManager != null){
            mWifiManager.setWifiEnabled(false);
        }
    }

    private void wifiScan(){
        if(mWifiManager != null){
            mWifiManager.startScan();
        }
    }

    private void wifiScanResult(){
        List<ScanResult> resultList = mWifiManager.getScanResults();
        LogUtils.LogD(TAG,"wifiScanResult "+mWifiManager.getScanResults().toString());
        Collections.sort(resultList, new Comparator<ScanResult>() {
            public int compare(ScanResult s1, ScanResult s2) {
                return s2.level - s1.level;
            }
        });
        if ((resultList != null) && (!resultList.isEmpty())) {
            String str3 = getString(R.string.wifi_test_find);
            StringBuilder sb = new StringBuilder().append(str3).append("\n");
            ScanResult selectAp = null;
            for (ScanResult scanResult : resultList) {
                sb.append(scanResult.SSID
                                + "\t- "
                                + scanResult.capabilities
                                + "\t- level:"
                                + WifiManager.calculateSignalLevel(
                                scanResult.level, 4)).append("\n");
                if (scanResult.capabilities.length() < 6) {
                    if (null == selectAp || selectAp.level < scanResult.level) {
                        selectAp = scanResult;
                    }
                }
            }
            mResultTv.setText(sb.toString());
            mReadyToTest = true;
            if (mWifiManager.getConnectionInfo().getIpAddress() != 0) {
                LogUtils.LogD(TAG, "--already connect to:" + mWifiManager.getConnectionInfo().getSSID());
                mInfoTv.setText(getString(R.string.wifi_test_connect) + mWifiManager.getConnectionInfo().getSSID());
                return;
            }
            LogUtils.LogD(TAG, "--selected ap:" + selectAp);
            if (null == selectAp) {
                mInfoTv.setText(getString(R.string.wifi_test_connect_error));
                return;
            }

            int networkId = getNetworkId(selectAp.BSSID, selectAp.SSID);
            mInfoTv.setText(getString(R.string.wifi_test_try) + " " + selectAp.SSID);
            LogUtils.LogD(TAG, "--try connect to ap:" + selectAp.SSID);
            mWifiManager.enableNetwork(networkId, true);
        }
    }

    private int getNetworkId(String BSSID, String SSID) {
        for (WifiConfiguration wifiConfiguration : mWifiManager
                .getConfiguredNetworks()) {
            if (BSSID.equals(wifiConfiguration.BSSID)) {
                LogUtils.LogD(TAG, "--get existed config:" + wifiConfiguration.SSID);
                return wifiConfiguration.networkId;
            }
        }

        WifiConfiguration wc = new WifiConfiguration();
        wc.BSSID = BSSID;
        wc.SSID = "\"" + SSID + "\"";
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.networkId = mWifiManager.addNetwork(wc);
        LogUtils.LogD(TAG, "--new config:" + wc.SSID);
        return wc.networkId;
    }

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            LogUtils.LogD(TAG, "handleMessage -- msg.what:"+msg.what+" arg1:"+msg.arg1);
            switch (msg.what){
                case MessageUtils.MSG_WIFI_STATE_CHANGED:
                    if(msg.arg1 == WifiManager.WIFI_STATE_ENABLED){
                        wifiScan();
                    }
                    break;
                case MessageUtils.MSG_SUPPLICANT_CONNECTION_CHANGE:
                    if ((boolean)msg.obj && mReadyToTest) {
                        LogUtils.LogD(TAG, "already connect to:" + mWifiManager.getConnectionInfo().getSSID());
                        mInfoTv.setText("connect to " + mWifiManager.getConnectionInfo().getSSID());
                    }
                    break;
                case MessageUtils.MSG_NETWORK_STATE_CHANGED:
                    LogUtils.LogD(TAG, "getDetailedState : " + ((NetworkInfo)msg.obj).getDetailedState());
                    switch(((NetworkInfo)msg.obj).getDetailedState()) {
                        case CONNECTED:
                            LogUtils.LogD(TAG, "connect to:" + mWifiManager.getConnectionInfo().getSSID());
                            mInfoTv.setText("Connected to " + mWifiManager.getConnectionInfo().getSSID());
                            break;
                    }
                    break;
                case MessageUtils.MSG_SCAN_RESULTS_AVAILABLE:
                    wifiScanResult();
                    break;
            }
        }
    };
}
