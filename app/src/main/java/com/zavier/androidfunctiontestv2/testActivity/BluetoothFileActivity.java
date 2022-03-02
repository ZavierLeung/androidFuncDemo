package com.zavier.androidfunctiontestv2.testActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.bluetoothTest.ble.BleClient;
import com.zavier.androidfunctiontestv2.bluetoothTest.ble.BleServer;
import com.zavier.androidfunctiontestv2.bluetoothTest.bt.BtBase;
import com.zavier.androidfunctiontestv2.bluetoothTest.bt.BtClient;
import com.zavier.androidfunctiontestv2.bluetoothTest.bt.BtReceiver;
import com.zavier.androidfunctiontestv2.bluetoothTest.bt.BtServer;
import com.zavier.androidfunctiontestv2.customAdapter.BlueToothAdapter;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.FileUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.lang.ref.WeakReference;


public class BluetoothFileActivity extends AppCompatActivity implements View.OnClickListener,
        BlueToothAdapter.OnItemClickListener, BtReceiver.Listener, BtBase.Listener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "BluetoothFileActivity";
    private Button mBtClientBtn, mBleClientBtn, mBtServertBtn, mBleServertBtn;
    private TextView mResultTv;
    private TextView mShowTV, mStateTV;
    private Button mSendBtn, mSelectBtn, mScanBtn, mReturnBtn;
    private RadioGroup mSendTypeRG;
    private EditText mSendEdit;
    private RecyclerView mRecyclerView;
    private LinearLayout mSelectModeLLayout, mSecondLLayout, mSencondSelectFileLLyout;
    private TextViewTitleUtils mTitleUtile;
    private int mType = -1;
    public static final int TYPE_BT_CLIENT      = 0;
    public static final int TYPE_BLE_CLIENT     = 1;
    public static final int TYPE_BT_SERVER      = 2;
    public static final int TYPE_BLE_SERVER     = 3;
    public final MyHandler handler = new MyHandler(this);
    private BlueToothAdapter mBlueToothAdapter;
    private BtClient mBtClient;
    private BtServer mBtServer;
    private BtReceiver mBtReceiver;
    private BleClient mBleClient;
    private BleServer mBleServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_file);
        initView();
        initSelectModeView();
        checkBluetooth();
        initSecondView();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this, true, false);
        mTitleUtile.setTitle(R.string.main_func_bluetooth_file);
        mSelectModeLLayout = findViewById(R.id.bluetooth_file_select_mode_llayout);
        mSecondLLayout = findViewById(R.id.bluetooth_file_llayout);
        mSencondSelectFileLLyout = findViewById(R.id.bluetooth_second_type_ll);
    }

    private void setSecondLayout(boolean b){
        if(b){
            mSelectModeLLayout.setVisibility(View.GONE);
            mSecondLLayout.setVisibility(View.VISIBLE);
            mRecyclerView = findViewById(R.id.bluetooth_second_send_rv);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mBlueToothAdapter = new BlueToothAdapter(this, R.layout.item_bluetooth_dev, mType, this);
            mRecyclerView.setAdapter(mBlueToothAdapter);
            mSendEdit.setText("");
            mStateTV.setText("");
            mShowTV.setText("");
            if(mType == TYPE_BT_SERVER || mType == TYPE_BLE_SERVER){
                mScanBtn.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mScanBtn.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            }
            if(mType == TYPE_BLE_CLIENT || mType == TYPE_BLE_SERVER){
                mSencondSelectFileLLyout.setVisibility(View.GONE);
            } else {
                mSencondSelectFileLLyout.setVisibility(View.VISIBLE);
            }
        } else {
            mSelectModeLLayout.setVisibility(View.VISIBLE);
            mSecondLLayout.setVisibility(View.GONE);
        }
    }

    private void initSelectModeView(){
        setSecondLayout(false);
        mBtClientBtn = findViewById(R.id.bt_client_btn);
        mBleClientBtn = findViewById(R.id.ble_client_btn);
        mBtServertBtn = findViewById(R.id.bt_server_btn);
        mBleServertBtn = findViewById(R.id.ble_server_btn);
        mResultTv = findViewById(R.id.bluetooth_state);

        mBtClientBtn.setOnClickListener(this);
        mBleClientBtn.setOnClickListener(this);
        mBtServertBtn.setOnClickListener(this);
        mBleServertBtn.setOnClickListener(this);
    }

    private void initSecondView(){
        mShowTV = findViewById(R.id.bluetooth_second_message_tv);
        mSendBtn = findViewById(R.id.bluetooth_second_send_btn);
        mSelectBtn = findViewById(R.id.bluetooth_second_select_file_btn);
        mSendTypeRG = findViewById(R.id.bluetooth_second_send_type_rg);
        mScanBtn = findViewById(R.id.bluetooth_second_scan_btn);
        mSendEdit = findViewById(R.id.bluetooth_second_message_et);
        mStateTV = findViewById(R.id.bluetooth_second_state_tv);
        mReturnBtn = findViewById(R.id.bluetooth_second_return_btn);
        mShowTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        mSelectBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mScanBtn.setOnClickListener(this);
        mReturnBtn.setOnClickListener(this);
        mSendTypeRG.setOnCheckedChangeListener(this);
        mSelectBtn.setEnabled(false);
    }

    private void setTitle(boolean subTitle, int id){
        if(subTitle){
            mTitleUtile.setTitleVisibility(View.GONE);
            mTitleUtile.setSubTitle(id);
        } else {
            mTitleUtile.setTitleVisibility(View.VISIBLE);
            mTitleUtile.setSubTitle("");
        }
    }

    private void initBtClient(){
        mType = TYPE_BT_CLIENT;
        setTitle(true, R.string.bluetooth_bt_client);
        mBtReceiver = new BtReceiver(this,this,handler);
        mBtClient = new BtClient(this);
        setSecondLayout(true);
    }

    private void initBleClient(){
        mType = TYPE_BLE_CLIENT;
        setTitle(true, R.string.bluetooth_ble_client);
        mBleClient = new BleClient(this,handler);
        setSecondLayout(true);
    }

    private void initBtServer(){
        mType = TYPE_BT_SERVER;
        setTitle(true, R.string.bluetooth_bt_server);
        mBtServer = new BtServer(this,handler);
        setSecondLayout(true);
    }

    private void initBleServer(){
        mType = TYPE_BLE_SERVER;
        setTitle(true, R.string.bluetooth_ble_server);
        mBleServer = new BleServer(this,handler);
        setSecondLayout(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_client_btn:
                initBtClient();
                break;
            case R.id.ble_client_btn:
                initBleClient();
                break;
            case R.id.bt_server_btn:
                initBtServer();
                break;
            case R.id.ble_server_btn:
                initBleServer();
                break;
            case R.id.bluetooth_second_return_btn:
                setSecondLayout(false);
                releaseBluetooth();
                setTitle(false, 0);
                break;
            case R.id.bluetooth_second_send_btn:
                sendMsg();
                break;
            case R.id.bluetooth_second_select_file_btn:
                selectFile();
                break;
            case R.id.bluetooth_second_scan_btn:
                reScan();
                break;
        }
    }

    private void checkBluetooth(){
        // 检查蓝牙开关
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            mResultTv.setText("本机没有找到蓝牙硬件或驱动！");
            finish();
            return;
        } else {
            if (!adapter.isEnabled()) {
                //直接开启蓝牙
                adapter.enable();
                mBtClientBtn.setEnabled(false);
                mBleClientBtn.setEnabled(false);
                mBtServertBtn.setEnabled(false);
                mBleServertBtn.setEnabled(false);
            }
        }

        // 检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mResultTv.append("本机不支持低功耗蓝牙！");
            mBleServertBtn.setEnabled(false);
            mBleClientBtn.setEnabled(false);
        }
        if(BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser() == null){
            mResultTv.append("本机不支持低功耗蓝牙（服务器）！");
            mBleServertBtn.setEnabled(false);
        }
    }

    private void sendMsg(){
        if(TextUtils.isEmpty(mSendEdit.getText().toString())){
            return;
        }
        switch (mType){
            case TYPE_BT_CLIENT:
                if(mBtClient.isConnected(null)){
                    if(mSendTypeRG.getCheckedRadioButtonId() == R.id.bluetooth_second_file_rbtn){
                        mBtClient.sendFile(mSendEdit.getText().toString());
                    } else {
                        mBtClient.sendMsg(mSendEdit.getText().toString()+"\n");
                    }
                }
                break;
            case TYPE_BLE_CLIENT:
                mBleClient.write(mSendEdit.getText().toString()+"\n");
                break;
            case TYPE_BT_SERVER:
                if(mBtServer.isConnected(null)){
                    if(mSendTypeRG.getCheckedRadioButtonId() == R.id.bluetooth_second_file_rbtn){
                        mBtServer.sendFile(mSendEdit.getText().toString());
                    } else {
                        mBtServer.sendMsg(mSendEdit.getText().toString()+"\n");
                    }
                }
                break;
            case TYPE_BLE_SERVER:
                mBleServer.write("BleServer#"+mSendEdit.getText().toString()+"\n");
                break;
        }
        mSendEdit.setText("");
    }

    private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    mSendEdit.setText(FileUtils.getPath(this, uri));
                } else {//4.4以下系统调用方法
                    mSendEdit.setText(FileUtils.getRealPathFromURI(this, uri));
                }
            }
        }
    }

    // 重新扫描
    public void reScan() {
        switch (mType){
            case TYPE_BT_CLIENT:
                mBlueToothAdapter.reScanBt();
                break;
            case TYPE_BLE_CLIENT:
                mBlueToothAdapter.reScanBle();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        LogUtils.LogD(TAG,"onDestroy");
        super.onDestroy();
        releaseBluetooth();
    }


    private void releaseBluetooth(){
        switch (mType){
            case TYPE_BT_CLIENT:
                unregisterReceiver(mBtReceiver);
                mBtClient.unListener();
                mBtClient.close();
                break;
            case TYPE_BLE_CLIENT:
                mBleClient.closeConn();
                break;
            case TYPE_BT_SERVER:
                mBtServer.unListener();
                mBtServer.close();
                break;
            case TYPE_BLE_SERVER:
                mBleServer.closeBleServer();
                break;
        }
        mType = -1;
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        switch (mType){
            case TYPE_BT_CLIENT:
                if (mBtClient.isConnected(dev)) {
                    LogUtils.LogD(TAG,"已经连接了");
                    return;
                }
                LogUtils.LogD(TAG,"bt client connecting");
                mBtClient.connect(dev);
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS,"正在连接...\n");
                break;
            case TYPE_BLE_CLIENT:
                mBleClient.closeConn();
                mBleClient.setBluetoothGatt(dev.connectGatt(this, false, mBleClient.mBluetoothGattCallback));
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS,String.format("与[%s]开始连接............\n", dev));
//                mShowTV.setText(String.format("与[%s]开始连接............", dev));
                break;
        }
    }

    @Override
    public void foundBtDev(BluetoothDevice dev) {
        mBlueToothAdapter.addBt(dev);
    }

    @Override
    public void socketNotify(int state, Object obj) {
        if (isDestroyed())
            return;
        String date = LogUtils.getTimeDateFormat();
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("["+ date + "] 与%s(%s)连接成功\n", dev.getName(),
                        dev.getAddress());
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS,msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                if(mType == TYPE_BT_SERVER){
                    mBtServer.listen();
                }
                msg = "["+ date + "] 连接断开,正在重新监听...\n";
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS,msg);
                break;
            case BtBase.Listener.RECEIVE_MSG:
                String string = (String) obj;
                Log.d(TAG,"string:"+string);
                msg = String.format("[" + date + "] %s\n",string);
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_MESSAGE_RECEIVE,msg);
//                mShowTV.append(msg);
                SystemClock.sleep(1000);
                break;
            case BtBase.Listener.RECEIVE_FILE_START:
            case BtBase.Listener.RECEIVE_FILE_END:
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_MESSAGE_RECEIVE,obj.toString());
                break;
            case BtBase.Listener.SEND_FILE_START:
            case BtBase.Listener.SEND_FILE_END:
                MessageUtils.sendMessage(handler, MessageUtils.MSG_BLUETOOTH_MESSAGE_SEND,obj.toString());
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if(radioGroup.getCheckedRadioButtonId() == R.id.bluetooth_second_file_rbtn){
            mSelectBtn.setEnabled(true);
        } else {
            mSelectBtn.setEnabled(false);
        }
    }

    static class MyHandler extends Handler{
        WeakReference<BluetoothFileActivity> weakReference;
        public MyHandler(BluetoothFileActivity activity) {
            weakReference = new WeakReference<BluetoothFileActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (weakReference.get() != null) {
                switch (msg.what){
                    case MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS:
                    case MessageUtils.MSG_BLUETOOTH_CONNECT_FAILED:
                    case MessageUtils.MSG_BLUETOOTH_CONNECT_SERVER:
                    case MessageUtils.MSG_BLUETOOTH_SERVER_START:
                        weakReference.get().mStateTV.setText(msg.obj.toString());
                        break;
                    case MessageUtils.MSG_BLUETOOTH_DISCOVERY_FINISHED:
                        weakReference.get().mStateTV.setText(msg.obj.toString());
                        weakReference.get().mScanBtn.setEnabled(true);
                        break;
                    case MessageUtils.MSG_BLUETOOTH_DISCOVERY_STARTED:
                        weakReference.get().mStateTV.setText(msg.obj.toString());
                        weakReference.get().mScanBtn.setEnabled(false);
                        break;
                    case MessageUtils.MSG_BLUETOOTH_MESSAGE_RECEIVE:
                    case MessageUtils.MSG_BLUETOOTH_MESSAGE_SEND:
                        weakReference.get().mShowTV.append(msg.obj.toString());
                        break;
                }
            }
        }
    }

}
