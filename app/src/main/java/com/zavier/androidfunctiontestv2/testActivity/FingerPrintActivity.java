package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

//import org.zz.api.MXComModuleAPI;

public class FingerPrintActivity extends AppCompatActivity {
    private static final String TAG = "FingerPrintActivity";
    private TextViewTitleUtils mTitleUtile;
    private TextView mResultTv;
  //  private MXComModuleAPI mxComModuleAPI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        initView();
        initFingerPrint();
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(FingerPrintActivity.this,true,true);
        mTitleUtile.setTitle(R.string.main_func_fingerprint);
        mTitleUtile.setSubTitle(R.string.finger_print_test_sub_title);

        mResultTv = findViewById(R.id.finger_print_result_tv);
    }

    private void initFingerPrint(){
       // mxComModuleAPI = new MXComModuleAPI();
        byte[] bytes = new byte[100];
       // int ret = mxComModuleAPI.zzGetDevVersion("/dev/ttyS4", 115200, bytes);
//        if(ret == 0){
//            mResultTv.setText(R.string.finger_print_test_success);
//        } else {
//            mResultTv.setText(R.string.finger_print_test_failed);
//        }
        mResultTv.append(new String(bytes));
    }
}
