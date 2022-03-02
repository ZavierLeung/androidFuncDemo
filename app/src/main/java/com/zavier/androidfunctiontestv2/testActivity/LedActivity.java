package com.zavier.androidfunctiontestv2.testActivity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.ControlButtonUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;
import com.zavier.androidfunctiontestv2.customUtils.TextViewTitleUtils;

import java.io.DataOutputStream;

public class LedActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG="LedActivity";
    private TextViewTitleUtils mTitleUtile;
    private Button mOpenBtn, mCloseBtn;

    /**
     * finger led control :  /sys/class/gpio/gpio150/value  值：1  or  0  注释：1代表高：灯亮，0 代表低：灯灭
     * ic_card led control :  /sys/class/gpio/gpio154/value  值：1  or  0 注释：1代表高：灯亮，0 代表低：灯灭
     * magnetic led control :  /sys/class/gpio/gpio155/value 值：1  or  0 注释：1代表高：灯亮，0 代表低：灯灭
     * nfc led control :  /sys/class/gpio/gpio156/value 值：1  or  0  注释：1代表高：灯亮，0 代表低：灯灭
     * */
    private final static String SATA_LED1 = "/sys/class/gpio/gpio150/value";    //指纹指示灯
    private final static String SATA_LED2 = "/sys/class/gpio/gpio154/value";    //IC卡指示灯
    private final static String SATA_LED3 = "/sys/class/gpio/gpio155/value";    //磁条卡指示灯
    private final static String SATA_LED4 = "/sys/class/gpio/gpio156/value";    //非接指示灯

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        control_all_led(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        control_all_led(0);
    }

    private void initView(){
        new ControlButtonUtils(this, ParametersUtils.getClassId(this.getClass()));
        mTitleUtile = new TextViewTitleUtils(LedActivity.this, true, false);
        mTitleUtile.setTitle(R.string.main_func_led);
        mTitleUtile.setSubTitle(R.string.led_test_sub_title);

        mOpenBtn = findViewById(R.id.led_open_btn);
        mCloseBtn = findViewById(R.id.led_close_btn);
        mOpenBtn.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.led_open_btn:
                control_all_led(1);
                break;
            case R.id.led_close_btn:
                control_all_led(0);
                break;
        }
    }

    private void control_all_led(int on_off){
        control_led(on_off, SATA_LED1);
        control_led(on_off, SATA_LED2);
        control_led(on_off, SATA_LED3);
        control_led(on_off, SATA_LED4);
    }

    private void control_led(int on_off, String sata) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String command = "echo " + on_off + " > " + sata;
            LogUtils.LogD(TAG,command);
//            process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c", command});
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.d("runtime_exception:", e.getMessage());
        } finally {
            try {
                if (os != null)
                    os.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
