package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.SerialPortTest.SerialPort;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SerialPortActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "SerialPortActivity";
    private TextViewTitleUtils mTitleUtile;
    private EditText mOpenEdit, mSendEdit;
    private TextView mReceiveTV;
    private Button mOpenBtn, mSendBtn, mReceiveBtn;
    private SerialPort mSerialPort;
    private Spinner mSpinner;



    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MessageUtils.MSG_SERIAL_OPEN:
                    mReceiveTV.setText(msg.obj.toString()+"\n");
                    break;
                case MessageUtils.MSG_SERIAL_SEND:
                case MessageUtils.MSG_SERIAL_RECEIVE:
                    mReceiveTV.append(msg.obj.toString()+"\n");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);
        initView();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(this);
        mTitleUtile.setTitle(R.string.main_func_serial_port);
        mOpenBtn = findViewById(R.id.serial_port_test_openBtn);
        mSendBtn = findViewById(R.id.serial_port_test_sendBtn);
        mSendEdit = findViewById(R.id.serial_port_test_sendET);
        mOpenEdit = findViewById(R.id.serial_port_test_openET);
        mReceiveTV = findViewById(R.id.serial_port_test_receiveTV);
        mReceiveBtn = findViewById(R.id.serial_port_test_receiveBtn);
        mSpinner = findViewById(R.id.serial_port_test_spinner);

        mSendBtn.setOnClickListener(this);
        mOpenBtn.setOnClickListener(this);
        mReceiveBtn.setOnClickListener(this);
        setSendView(false);
        initSpinner();
    }

    private void setOpenView(boolean b){
        mOpenEdit.setEnabled(b);
        mOpenBtn.setEnabled(b);
        mSpinner.setEnabled(b);
    }
    private void setSendView(boolean b){
        mSendBtn.setEnabled(b);
        mSendEdit.setEnabled(b);
        mReceiveBtn.setEnabled(b);
    }

    private void initSpinner(){
        File file = new File("/dev");
        File[] files = file.listFiles();
        if(files == null){
            return;
        }

        //spinner数据
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add(getResources().getString(R.string.serial_port_test_spinner));

        for(int i = 0; i < files.length; i++){
            data_list.add(files[i].getPath());
        }
        //适配器
        ArrayAdapter<String> arr_adapter= new ArrayAdapter<String>(this, R.layout.spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        //加载适配器
        mSpinner.setAdapter(arr_adapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    private void openSerailPort(){
        try {
            String string = null;
            if(mSpinner.getSelectedItemPosition() != 0){
                string = mSpinner.getSelectedItem().toString();
            } else {
                string = mOpenEdit.getText().toString();
            }
            if(!string.isEmpty()){
                mSerialPort = new SerialPort(new File(string));
                if(mSerialPort.isEmpty()){
                    MessageUtils.sendMessage(mHandler, MessageUtils.MSG_SERIAL_OPEN,string+" open fail");
                } else {
                    MessageUtils.sendMessage(mHandler, MessageUtils.MSG_SERIAL_OPEN,string+" open success");
                    setOpenView(false);
                    setSendView(true);
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendData(){
        String string = mSendEdit.getText().toString();
        if(!string.isEmpty()){
            mSerialPort.write(string.getBytes(),string.length());
            MessageUtils.sendMessage(mHandler, MessageUtils.MSG_SERIAL_SEND, "send: "+string);
            mSendEdit.setText("");
        }
    }

    private void receiveData(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = null;
                buffer = mSerialPort.read();
                if(buffer == null){
                    return ;
                }
                Log.d("receiveData","buffer: "+new String(buffer)+" size:"+buffer.length);
                if (buffer.length > 0) {
                    MessageUtils.sendMessage(mHandler, MessageUtils.MSG_SERIAL_RECEIVE, "receive: " + new String(buffer));
                }
            }
        });
        thread.start();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.serial_port_test_openBtn:
                openSerailPort();
                break;
            case R.id.serial_port_test_sendBtn:
                sendData();
                break;
            case R.id.serial_port_test_receiveBtn:
                receiveData();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == 0){
            mOpenEdit.setVisibility(View.VISIBLE);
        } else {
            mOpenEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
