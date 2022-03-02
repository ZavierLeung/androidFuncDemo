package com.zavier.androidfunctiontestv2.customAdapter;

import android.content.Context;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.MainApplication;
import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customSql.SqlHelper;
import com.zavier.androidfunctiontestv2.customSql.SqlStaticData;
import com.zavier.androidfunctiontestv2.customUtils.LoadTestUtils;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;
import com.zavier.androidfunctiontestv2.customUtils.ParametersUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private Context mContext;
    private int mItemCount = 12;
    private int mResoureLayoutId = 0;
    private OnItemClickListener mOnItemClickListener;
    private OnItemCheckListener mOnItemCheckListener;
    private SqlHelper mSqlHelper = MainApplication.getSqlHelper();
    private int mType = -1;
    private List<Sensor> mSensorList;

    public RecyclerViewAdapter(Context context, int id, int itemCount,
                               int type, OnItemClickListener onItemClickListener){
        this.mContext = context;
        this.mResoureLayoutId = id;
        this.mItemCount = itemCount;
        this.mOnItemClickListener = onItemClickListener;
        this.mType = type;
    }

    public RecyclerViewAdapter(Context context, List<Sensor> list, int id, int itemCount,
                               int type, OnItemCheckListener onItemCheckListener){
        this.mContext = context;
        this.mResoureLayoutId = id;
        this.mItemCount = itemCount;
        this.mOnItemCheckListener = onItemCheckListener;
        this.mType = type;
        this.mSensorList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(mContext).inflate(mResoureLayoutId,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (mType){
            case ParametersUtils.TYPE_MAIN_FUNC:
                int flag = MessageUtils.FLAG_TEST_READY;
                if(mSqlHelper != null){
                    ArrayList<SqlStaticData> list = mSqlHelper.queryAll();
                    flag = list.get(position).funcStatus;
                    long id = list.get(position).rowId;
                    LogUtils.LogD(TAG, "onBindViewHolder status: " + flag + " id: "+ id);
                }
                LoadTestUtils.loadMainFunc(mContext, (RecyclerViewHolder)holder, position, flag);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mOnItemClickListener != null){
                            mOnItemClickListener.onClick(view, position);
                        }
                    }
                });
                break;
            case ParametersUtils.TYPE_SENSOR_TEST:
                LoadTestUtils.loadSensor(mContext, (RecyclerViewHolder)holder, position, mSensorList);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mOnItemCheckListener != null){
                            boolean check = ((RecyclerViewHolder) holder).mSensorCb.isChecked();
                            ((RecyclerViewHolder) holder).mSensorCb.setChecked(!check);
                            mOnItemCheckListener.onCheck(view, position,mSensorList.get(position),
                                    ((RecyclerViewHolder) holder));
                        }
                    }
                });
                break;

        }

    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public interface OnItemClickListener{
        void onClick(View view, int pos);
    }

    public interface OnItemCheckListener{
        void onCheck(View view, int pos, Sensor sensor,
                     RecyclerViewAdapter.RecyclerViewHolder viewHolder);
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{
        public TextView mMainTv;
        public ImageView mImageView;
        public TextView mSensorTv, mResultTv2;
        public CheckBox mSensorCb;
        public LinearLayout mSensorLLayout1, mSensorLLayout2;

        public RecyclerViewHolder(View view){
            super(view);
            switch (mType){
                case ParametersUtils.TYPE_MAIN_FUNC:
                    mMainTv = view.findViewById(R.id.recycler_main_func_tv);
                    mImageView = view.findViewById(R.id.recycler_main_func_iv);
                    break;
                case ParametersUtils.TYPE_SENSOR_TEST:
                    mSensorTv = view.findViewById(R.id.recycler_sensor_name_tv);
                    mSensorCb = view.findViewById(R.id.recycler_sensor_cb);
                    mSensorLLayout1 = view.findViewById(R.id.recycler_sensor_result_tv1);
                    mSensorLLayout2 = view.findViewById(R.id.recycler_sensor_result_tv2);
                    mResultTv2  = mSensorLLayout2.findViewById(R.id.sensor_result_tv);
                    mResultTv2.setText(R.string.sensor_test_result);
                    mResultTv2.append("2");
                    break;
            }
        }

        public void showSensorLLayout(boolean layout1, boolean layout2){
            if(layout1){
                mSensorLLayout1.setVisibility(View.VISIBLE);
            } else {
                mSensorLLayout1.setVisibility(View.GONE);
            }
            if(layout2){
                mSensorLLayout2.setVisibility(View.VISIBLE);
            } else {
                mSensorLLayout2.setVisibility(View.GONE);
            }
        }

        public void setSensor1XYZ(String x,String y,String z) {
            if (mSensorLLayout1 != null) {
                ((TextView) mSensorLLayout1.findViewById(R.id.sensor_x_result_tv)).setText(x);
                ((TextView) mSensorLLayout1.findViewById(R.id.sensor_y_result_tv)).setText(y);
                ((TextView) mSensorLLayout1.findViewById(R.id.sensor_z_result_tv)).setText(z);
            }
        }


        public void setSensor2XYZ(String x,String y,String z){
            if (mSensorLLayout2 != null) {
                ((TextView) mSensorLLayout2.findViewById(R.id.sensor_x_result_tv)).setText(x);
                ((TextView) mSensorLLayout2.findViewById(R.id.sensor_y_result_tv)).setText(y);
                ((TextView) mSensorLLayout2.findViewById(R.id.sensor_z_result_tv)).setText(z);
            }
        }

        public void setSensorName(String string){
            if(mSensorTv != null){
                mSensorTv.setText(string);
            }
        }

        public void setMainFunTv(int id){
            if(mMainTv != null){
                mMainTv.setText(id);
            }
        }

        public void setImageView(int flag){
            if(mImageView == null){
                return ;
            }
            switch (flag){
                case MessageUtils.FLAG_TEST_SUCCESS:
                    mImageView.setBackgroundResource(R.drawable.ic_pass);
                    break;
                case MessageUtils.FLAG_TEST_FAIL:
                    mImageView.setBackgroundResource(R.drawable.ic_no_pass);
                    break;
                case MessageUtils.FLAG_TEST_READY:
                    mImageView.setBackgroundResource(R.drawable.ic_ready_test);
                    break;
            }
        }

    }
}
