package com.zavier.androidfunctiontestv2.testActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
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

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    private TextViewTitleUtils mTitleUtile;
    private TextView mTextView;
    private BluetoothAdapter mAdapter;
    private ReceiverUtils mReceiverUtils;
    private int mTestOpen = 0;
    private int mTestCount = 0;
    private int mMaxTest = 10;
    private boolean isTestFinish = false;
    private StringBuilder mStringBuilder = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        openBluetooth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeBluetooth();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(BluetoothActivity.this);
        mTitleUtile.setTitle(R.string.main_func_bluetooth);
        mTextView = findViewById(R.id.bluetooth_test_tv);
    }

    private void openBluetooth(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAdapter != null){
            initReceiver();
            if (mAdapter.isEnabled()) {
                mAdapter.startDiscovery();
                mTextView.setText(getString(R.string.bluetooth_test_open_success));
                mTextView.append("\n"+getString(R.string.bluetooth_test_scan));
            } else {
                reopenBluetooth();
                mTextView.setText(getString(R.string.bluetooth_test_init));
            }
        } else {
            LogUtils.LogE(TAG, "openBluetooth mAdapter is null");
            mTextView.setText(getString(R.string.bluetooth_test_adapter_fail));
        }
    }

    private void initReceiver(){
        mReceiverUtils = new ReceiverUtils(mHandler,this.getClass());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiverUtils, intentFilter);
    }

    private void closeReceiver(){
        if(mReceiverUtils != null){
            unregisterReceiver(mReceiverUtils);
        }
    }

    private void closeBluetooth(){
        if (this.mAdapter != null) {
            mAdapter.cancelDiscovery();
            mAdapter.disable();
        }
        closeReceiver();
        isTestFinish = false;
    }

    private void reopenBluetooth(){
        LogUtils.LogD(TAG, "reopenBluetooth mAdapter.isEnabled()" + mAdapter.isEnabled());
        if (mAdapter.isEnabled()) {
            LogUtils.LogD(TAG, "reopenBluetooth,bluetooth is open and then startDiscovery!");
            mAdapter.startDiscovery();
        } else {
            if (mTestOpen < mMaxTest) {
                LogUtils.LogD(TAG, "bluetooth is close and then open it! mTestOpen=" + mTestOpen);
                mTestOpen++;
                mAdapter.enable();
            } else {
                mTextView.setText(getString(R.string.bluetooth_test_open_fail));
            }
        }
    }

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            LogUtils.LogD(TAG, "handleMessage -- msg.what:"+msg.what);
            switch (msg.what){
                case MessageUtils.MSG_BLUETOOTH_STATE_CHANGED:
                    if(msg.arg1 == BluetoothAdapter.STATE_ON){
                        LogUtils.LogD(TAG, "handleMessage -- STATE_ON");
                        mAdapter.startDiscovery();
                    } else if(msg.arg1 == BluetoothAdapter.STATE_OFF){
                        LogUtils.LogD(TAG, "handleMessage -- STATE_OFF");
                    }
                    break;
                case MessageUtils.MSG_BLUETOOTH_DISCOVERY_STARTED:
                    LogUtils.LogD(TAG, "handleMessage -- ACTION_DISCOVERY_STARTED mTestCount=" + mTestCount);
                    mTextView.setText(getString(R.string.bluetooth_test_open_success));
                    mTextView.append("\n"+getString(R.string.bluetooth_test_scan));
                    mTextView.append("\n"+getString(R.string.bluetooth_test_scan_num)+mTestCount);
                    break;
                case MessageUtils.MSG_BLUETOOTH_DISCOVERY_FINISHED:
                    LogUtils.LogD(TAG, "handleMessage -- ACTION_DISCOVERY_FINISHED");
                    if(isTestFinish) return;
                    if (mTestCount < mMaxTest ) {
                        mTestCount++;
                        mAdapter.startDiscovery();
                    } else {
                        mTextView.setText(getString(R.string.bluetooth_test_find_fail));
                    }
                    break;
                case MessageUtils.MSG_BLUETOOTH_FOUND:
                    BluetoothDevice device = (BluetoothDevice)msg.obj;
                    if(device != null){
                        isTestFinish = true;
                        StringBuilder sb = new StringBuilder().append(getString(R.string.bluetooth_test_find_success)).append(":");
                        sb.append("     \t- name :" + device.getName());
                        sb.append("     \t- address :" + device.getAddress());
                        sb.append("\n");
                        mStringBuilder.append(sb.toString());
                        mTextView.setText(mStringBuilder.toString());
                    } else {
                        LogUtils.LogE(TAG, "handleMessagedevice device is null");
                    }
                    break;
            }
        }
    };

}
