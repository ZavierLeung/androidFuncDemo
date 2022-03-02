package com.zavier.androidfunctiontestv2.testActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.FileUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;
import com.zavier.androidfunctiontestv2.usbCommunication.aoa.UsbStateManager;
import com.zavier.androidfunctiontestv2.usbCommunication.hid.OpenUsbDevice;
import com.zavier.androidfunctiontestv2.usbCommunication.hid.OpenUsbHidPort;
import com.zavier.androidfunctiontestv2.usbCommunication.hid.OpenUsbHost;
import com.zavier.androidfunctiontestv2.usbCommunication.hid.UsbDetachedReceiver;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zavier.androidfunctiontestv2.customUtils.MessageUtils.USB_AOA_ACCESSORY;
import static com.zavier.androidfunctiontestv2.customUtils.MessageUtils.USB_AOA_HOST;
import static com.zavier.androidfunctiontestv2.customUtils.MessageUtils.USB_HID_DEVICE;
import static com.zavier.androidfunctiontestv2.customUtils.MessageUtils.USB_HID_HOST;

public class UsbCommunicationActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, UsbDetachedReceiver.UsbDetachedListener {
    private static final String TAG = "UsbCommunicationActivity";
    private TextViewTitleUtils mTitleUtile;
    private RadioGroup mSendTypeRG;
    private Button mSendBtn, mSelectFileBtn, mCleanBtn, mStartBtn;
    private Button mAoaHostBtn, mAoaDeviceBtn, mHidHostBtn, mHidDeviceBtn;
    private TextView mStatusTv, mMessageTv;
    private EditText mMessageEt, mVidEt, mPidEt;
    private ExecutorService mThreadPool;
    private LinearLayout mSelectModeLLayout, mSecondLLayout, mPidVidLLyout;
    private int mType = -1;
    private int mAoaSize = 1024 *10; //bulkTransfer不能超过16K
    private int mHidSize = 1024;
    private UsbStateManager mUsbStateManager;
    private String mFilePath;
    private UsbDetachedReceiver mUsbDetachedReceiver;
    private OpenUsbHost mOpenUsbHost;
    private OpenUsbDevice mOpenUsbDevice;

    public final MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_communication);

        mThreadPool = Executors.newFixedThreadPool(3);
        initView();
        initSelectModeView();
        initSecondView();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this,true,false);
        mTitleUtile.setTitle(R.string.main_func_usb_communication);
        mTitleUtile.setSubTitle(R.string.usb_communication_test_sub_title);
        mSelectModeLLayout = findViewById(R.id.usb_communication_select_mode_llayout);
        mSecondLLayout = findViewById(R.id.usb_communication_test_llayout);
        mPidVidLLyout = findViewById(R.id.usb_vid_pid_llayout);
    }

    private void initUsbManager(){
        mUsbStateManager = new UsbStateManager(this, mHandler, mType, new UsbStateManager.UsbStatus() {
            @Override
            public void usbDetached() {
                if(mType == MessageUtils.USB_AOA_HOST){
                    finish();
                }
            }
        });
    }

    private void initReceiver(){
        mUsbDetachedReceiver = new UsbDetachedReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(MessageUtils.USB_STATE);
        registerReceiver(mUsbDetachedReceiver, intentFilter);
    }

    private void initSelectModeView(){
        setSecondLayout(false);
        mAoaDeviceBtn = findViewById(R.id.aoa_accessory_btn);
        mAoaHostBtn = findViewById(R.id.aoa_host_btn);
        mHidDeviceBtn = findViewById(R.id.hid_device_btn);
        mHidHostBtn = findViewById(R.id.hid_host_btn);
        mHidHostBtn.setOnClickListener(this);
        mHidDeviceBtn.setOnClickListener(this);
        mAoaHostBtn.setOnClickListener(this);
        mAoaDeviceBtn.setOnClickListener(this);
    }

    private void initSecondView(){
        mCleanBtn = findViewById(R.id.usb_communication_test_clean_btn);
        mMessageEt = findViewById(R.id.usb_communication_test_message_et);
        mMessageTv = findViewById(R.id.usb_communication_test_message_tv);
        mStatusTv = findViewById(R.id.usb_communication_test_status_tv);
        mSendTypeRG = findViewById(R.id.usb_communication_test_send_type_rg);
        mSelectFileBtn = findViewById(R.id.usb_communication_test_select_file_btn);
        mSendBtn = findViewById(R.id.usb_communication_test_send_btn);
        mStartBtn = findViewById(R.id.usb_communication_test_start_btn);
        mVidEt = findViewById(R.id.usb_vid_et);
        mPidEt = findViewById(R.id.usb_pid_et);
//        mVidEt.setText("206d");
//        mPidEt.setText("0003");
        mVidEt.setText("1dfc");
        mPidEt.setText("89a6");
        mCleanBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mSelectFileBtn.setOnClickListener(this);
        mSendTypeRG.setOnCheckedChangeListener(this);
        mStartBtn.setOnClickListener(this);
        setViewBtn(false);
        mSelectFileBtn.setEnabled(false);
        mStatusTv.setText(getResources().getString(R.string.usb_communication_usb_disconnect));
    }

    private void setSecondLayout(boolean b){
        if(b){
            mSelectModeLLayout.setVisibility(View.GONE);
            mSecondLLayout.setVisibility(View.VISIBLE);
            mTitleUtile.setSubTitleVisibility(View.GONE);
        } else {
            mSelectModeLLayout.setVisibility(View.VISIBLE);
            mSecondLLayout.setVisibility(View.GONE);
            mTitleUtile.setSubTitleVisibility(View.VISIBLE);
        }
    }

    private void initAoaAccessory(){
        mType = USB_AOA_ACCESSORY;
        mTitleUtile.setTitle(R.string.usb_communication_test_aoa_accessory);
        setSecondLayout(true);
        initUsbManager();
    }

    private void initAoaHost(){
        mType = USB_AOA_HOST;
        mTitleUtile.setTitle(R.string.usb_communication_test_aoa_host);
        setSecondLayout(true);
        initUsbManager();
    }

    private void initHidHost(){
        mType = USB_HID_HOST;
        mTitleUtile.setTitle(R.string.usb_communication_test_hid_host);
        setSecondLayout(true);
        mPidVidLLyout.setVisibility(View.VISIBLE);
        mOpenUsbHost = new OpenUsbHost(this,mHandler);
        initReceiver();

    }

    private void initHidDevice(){
        mType = USB_HID_DEVICE;
        mTitleUtile.setTitle(R.string.usb_communication_test_hid_device);
        setSecondLayout(true);
        mStartBtn.setVisibility(View.VISIBLE);
        mOpenUsbDevice = new OpenUsbDevice(this,mHandler);
        initReceiver();
        mOpenUsbDevice.connectUsbHidHost();
        if(mOpenUsbDevice.getHidStatus()){
            mStartBtn.setEnabled(false);
        }
        setViewBtn(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.aoa_accessory_btn:
                initAoaAccessory();
                break;
            case R.id.aoa_host_btn:
                initAoaHost();
                break;
            case R.id.hid_device_btn:
                initHidDevice();
                break;
            case R.id.hid_host_btn:
                initHidHost();
                break;
            case R.id.usb_communication_test_clean_btn:
                mMessageTv.setText("");
                break;
            case R.id.usb_communication_test_select_file_btn:
                selectFile();
                break;
            case R.id.usb_communication_test_send_btn:
                sendBtn(mMessageEt.getText().toString());
                break;
            case R.id.usb_communication_test_start_btn:
                if(!mOpenUsbDevice.getHidStatus()){
                    mOpenUsbDevice.connectUsbHidHost();
                    if(mOpenUsbDevice.getHidStatus()){
                        mStartBtn.setEnabled(false);
                    }
                }
                break;
        }
    }

    private void sendBtn(final String messageContent){
        if (!TextUtils.isEmpty(messageContent)) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if(mSendTypeRG.getCheckedRadioButtonId() == R.id.usb_communication_test_message_rbtn){
                        LogUtils.LogD(TAG,"choose message type");
                        sendMsg(messageContent);
                    } else {
                        LogUtils.LogD(TAG,"choose file type");
                        sendFile();
                    }
                }
            });
        }
    }

    private void sendMsg(String messageContent){
        switch (mType){
            case USB_AOA_HOST:
                mUsbStateManager.sendMessageToSlave(messageContent);
                break;
            case USB_AOA_ACCESSORY:
                mUsbStateManager.sendMessageToHost(messageContent);
                break;
            case USB_HID_HOST:
                mOpenUsbHost.sendMessageToHid(messageContent);
                break;
            case USB_HID_DEVICE:
                mOpenUsbDevice.sendMessageToHost(messageContent);
                break;
        }
    }

    private void sendFile(){
        switch (mType){
            case USB_AOA_HOST:
                mUsbStateManager.sendFileToSlave(mFilePath,mAoaSize);
                break;
            case USB_AOA_ACCESSORY:
                mUsbStateManager.sendFileToHost(mFilePath,mAoaSize);
                break;
            case USB_HID_HOST:
                mOpenUsbHost.sendFileToHid(mFilePath,mHidSize);
                break;
            case USB_HID_DEVICE:
                mOpenUsbDevice.sendFileToHost(mFilePath,mHidSize);
                break;
        }
    }

    private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if(mUsbStateManager != null){
            mUsbStateManager.closeUsbManager();
            mUsbStateManager.stopService();
        }
        if(mType == USB_HID_HOST){
            mOpenUsbHost.closeHidHost();
            if(mUsbDetachedReceiver != null){
                unregisterReceiver(mUsbDetachedReceiver);
            }
        } else if(mType == USB_HID_DEVICE){
            mOpenUsbDevice.closeUsbDevice();
            if(mUsbDetachedReceiver != null){
                unregisterReceiver(mUsbDetachedReceiver);
            }
        }
    }

    private void setViewBtn(boolean b){
        mSendBtn.setEnabled(b);
        mMessageEt.setEnabled(b);
        mCleanBtn.setEnabled(b);
        mSendTypeRG.setEnabled(b);
        if(mSendTypeRG.getCheckedRadioButtonId() == R.id.usb_communication_test_file_rbtn){
            mSelectFileBtn.setEnabled(b);
        } else {
            mSelectFileBtn.setEnabled(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    String string = FileUtils.getPath(this, uri);
                    if(string.equals("error")){
                        Toast.makeText(this, FileUtils.mErrorString, Toast.LENGTH_LONG).show();
                        mMessageEt.setText("");
                        mFilePath = "";
                    } else {
                        mMessageEt.setText(string);
                        mFilePath = string;
                    }
                } else {//4.4以下系统调用方法
                    mMessageEt.setText(FileUtils.getRealPathFromURI(this, uri));
                    mFilePath = FileUtils.getRealPathFromURI(this,uri);
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if(radioGroup.getCheckedRadioButtonId() == R.id.usb_communication_test_file_rbtn){
            mSelectFileBtn.setEnabled(true);
        } else {
            mSelectFileBtn.setEnabled(false);
            mMessageEt.setText("");
            mFilePath = "";
        }
    }

    @Override
    public void usbHidDetached() {
        LogUtils.LogD(TAG," usbDetached !!!!!!");
        if(mType == USB_HID_HOST){
            mOpenUsbHost.closeHidHost();
            mPidEt.setEnabled(true);
            mVidEt.setEnabled(true);
            setViewBtn(false);
            MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_FAILED,
                    getResources().getString(R.string.usb_communication_usb_disconnect));
        }

    }

    @Override
    public void usbHidAttached() {
        LogUtils.LogD(TAG,"usbHidAttached type:"+mType);
        if(mType == USB_HID_HOST){
            final String string1 = mVidEt.getText().toString();
            final String string2 = mPidEt.getText().toString();
            if(!TextUtils.isEmpty(string1) && !TextUtils.isEmpty(string2)) {
                mOpenUsbHost.connectUsbHidDevice(Integer.valueOf(string1, 16),
                        Integer.valueOf(string2, 16));
                mPidEt.setEnabled(false);
                mVidEt.setEnabled(false);
                setViewBtn(true);
            }
        }
    }

    @Override
    public void usbHidState() {
        LogUtils.LogD(TAG,"usbHidState type:"+mType);
        if(mType == USB_HID_DEVICE){
            if(mOpenUsbDevice.getHidStatus()){
                mOpenUsbDevice.closeUsbDevice();
                MessageUtils.sendMessage(mHandler,MessageUtils.MSG_USB_CONNECTED_FAILED,
                        getResources().getString(R.string.usb_communication_usb_disconnect));
                mStartBtn.setEnabled(true);
            }
        }
    }

    static class MyHandler extends Handler{
        WeakReference<UsbCommunicationActivity> weakReference;
        public MyHandler(UsbCommunicationActivity activity) {
            weakReference = new WeakReference<UsbCommunicationActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (weakReference.get() != null) {
                switch (msg.what) {
                    case MessageUtils.MSG_USB_SEND_MESSAGE_SUCCESS://成功发送数据
                        weakReference.get().mMessageEt.setText("");
                        break;
                    case MessageUtils.MSG_USB_RECEIVER_MESSAGE_SUCCESS://成功接受到数据
                        weakReference.get().mMessageTv.append(msg.obj.toString());
                        break;
                    case MessageUtils.MSG_USB_CONNECTED_SUCCESS://连接成功
                        if(weakReference.get().mType == USB_HID_DEVICE){
                            weakReference.get().mStatusTv.setText(weakReference.get().getResources().getString(R.string.usb_communication_usb_hid_device_connect));
                        } else {
                            weakReference.get().mStatusTv.setText(weakReference.get().getResources().getString(R.string.usb_communication_usb_connect_success));
                        }
                        weakReference.get().setViewBtn(true);
                        break;
                    case MessageUtils.MSG_USB_CONNECTED_ERR:
                    case MessageUtils.MSG_USB_CONNECTED_FAILED:
                        weakReference.get().mStatusTv.setText(msg.obj.toString());
                        weakReference.get().setViewBtn(false);
                        break;
                    case MessageUtils.MSG_USB_SEND_FILE_SUCCESS:
                    case MessageUtils.MSG_USB_RECEIVER_FILE_SUCCESS:
                        weakReference.get().mMessageTv.setText(msg.obj.toString());
                        break;
                    case MessageUtils.MSG_USB_RECEIVER_FILE_START:
                        break;
                }
            }
        }
    }
}
