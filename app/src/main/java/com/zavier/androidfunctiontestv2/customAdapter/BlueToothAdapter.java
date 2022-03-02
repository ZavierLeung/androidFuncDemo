package com.zavier.androidfunctiontestv2.customAdapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zavier.androidfunctiontestv2.R;
import com.zavier.androidfunctiontestv2.customUtils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.zavier.androidfunctiontestv2.testActivity.BluetoothFileActivity.TYPE_BLE_CLIENT;
import static com.zavier.androidfunctiontestv2.testActivity.BluetoothFileActivity.TYPE_BT_CLIENT;

public class BlueToothAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = BlueToothAdapter.class.getSimpleName();
    private Context mContext;
    private int mResoureLayoutId = 0;
    private boolean isScanning;
    private final Handler mHandler = new Handler();
    private BlueToothAdapter.OnItemClickListener mOnItemClickListener;
    private final List<BluetoothDevice> mBtDevices = new ArrayList<>();
    private final List<BleDev> mBleDevices = new ArrayList<>();
    private int mType = -1;

    private final ScanCallback mScanCallback = new ScanCallback() {// 扫描Callback
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BleDev dev = new BleDev(result.getDevice(), result);
            if (!mBleDevices.contains(dev)) {
                mBleDevices.add(dev);
                notifyDataSetChanged();
                Log.i(TAG, "onScanResult: " + result); // result.getScanRecord() 获取BLE广播数据
            }
        }
    };

    public BlueToothAdapter(Context context, int id, int type, OnItemClickListener onItemClickListener){
        this.mContext = context;
        this.mResoureLayoutId = id;
        this.mOnItemClickListener = onItemClickListener;
        this.mType = type;
        switch (mType){
            case TYPE_BT_CLIENT:
                addBtBound();
                break;
            case TYPE_BLE_CLIENT:
                scanBle();
                break;
        }
    }

    private void addBtBound() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null)
            mBtDevices.addAll(bondedDevices);
    }

    public void addBt(BluetoothDevice dev) {
        if (mBtDevices.contains(dev))
            return;
        mBtDevices.add(dev);
        notifyDataSetChanged();
    }

    public void reScanBt() {
        mBtDevices.clear();
        addBtBound();
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (!bt.isDiscovering())
            bt.startDiscovery();
        notifyDataSetChanged();
    }

    // 重新扫描
    public void reScanBle() {
        mBleDevices.clear();
        notifyDataSetChanged();
        scanBle();
    }

    public boolean getScanState(){
        return isScanning;
    }

    // 扫描BLE蓝牙(不会扫描经典蓝牙)
    private void scanBle() {
        isScanning = true;
//        BluetoothAdapter bluetoothAdapter = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE).getDefaultAdapter();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类
        bluetoothLeScanner.startScan(mScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(mScanCallback); //停止扫描
                isScanning = false;
            }
        }, 3000);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RecyclerViewHolder(LayoutInflater.from(mContext).inflate(mResoureLayoutId,
                viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        LogUtils.LogD(TAG, "onBindViewHolder, mBtDevices=" + mBtDevices.size() +" mBleDevices:"+mBleDevices.size());

        switch (mType){
            case TYPE_BT_CLIENT:
                BluetoothDevice devBt = mBtDevices.get(i);
                String nameBt = devBt.getName();
                String addressBt = devBt.getAddress();
                int bondState = devBt.getBondState();
                ((RecyclerViewHolder)viewHolder).name.setText(nameBt == null ? "" : nameBt);
                ((RecyclerViewHolder)viewHolder).address.setText(String.format("%s (%s)", addressBt, bondState == 10 ? "未配对" : "配对"));
                break;
            case TYPE_BLE_CLIENT:
                BleDev devBle = mBleDevices.get(i);
                String nameBle = devBle.dev.getName();
                String addressBle = devBle.dev.getAddress();
//        holder.name.setText(String.format("%s, %s, Rssi=%s", name, address, dev.scanResult.getRssi()));
//        holder.address.setText(String.format("广播数据{%s}", dev.scanResult.getScanRecord()));
                ((RecyclerViewHolder)viewHolder).name.setText(String.format("name: %s", nameBle));
                ((RecyclerViewHolder)viewHolder).address.setText(String.format("address: %s", addressBle));
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(mType == TYPE_BT_CLIENT){
            return mBtDevices.size();
        } else if(mType == TYPE_BLE_CLIENT){
            return mBleDevices.size();
        }
        return 0;
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView name;
        final TextView address;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.bluetooth_name);
            address = itemView.findViewById(R.id.bluetooth_address);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            LogUtils.LogD(TAG,"onClick, getAdapterPosition=" + pos);
            LogUtils.LogD(TAG, "onClick, mBtDevices=" + mBtDevices.size() +" mBleDevices:"+mBleDevices.size());
            if(mType == TYPE_BT_CLIENT){
                if (pos >= 0 && pos < mBtDevices.size()){
                    mOnItemClickListener.onItemClick(mBtDevices.get(pos));
                }
            } else if(mType == TYPE_BLE_CLIENT){
                if (pos >= 0 && pos < mBleDevices.size()){
                    mOnItemClickListener.onItemClick(mBleDevices.get(pos).dev);
                }
            }

        }
    }

    public interface OnItemClickListener{
        void onItemClick(BluetoothDevice dev);
    }

    public static class BleDev {
        public BluetoothDevice dev;
        ScanResult scanResult;

        BleDev(BluetoothDevice device, ScanResult result) {
            dev = device;
            scanResult = result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BleDev bleDev = (BleDev) o;
            return Objects.equals(dev, bleDev.dev);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dev);
        }
    }

}
