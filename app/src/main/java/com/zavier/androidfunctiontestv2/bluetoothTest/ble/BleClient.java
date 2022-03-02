package com.zavier.androidfunctiontestv2.bluetoothTest.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.util.Arrays;
import java.util.UUID;

public class BleClient {
    public static final UUID UUID_SERVICE = UUID.fromString("00001111-0000-0000-8000-00801A2B3C4D"); //自定义UUID
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("00001112-0000-0000-8000-00801A2B3C4D");
    public static final UUID UUID_CHAR_WRITE = UUID.fromString("00001113-0000-0000-8000-00801A2B3C4D");
    public static final UUID UUID_DESC_NOTITY = UUID.fromString("00001114-0000-0000-8000-00801A2B3C4D");
    private static final String TAG = "BleClient";
    private Context mContext;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    public boolean isConnected = false;

    public BleClient(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
    }

    // 与服务端连接的Callback
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            LogUtils.LogD(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(),
                    dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                gatt.requestMtu(512);
                isConnected = true;
//                gatt.discoverServices(); //启动服务发现
            } else {
                isConnected = false;
                closeConn();
            }
            MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS, String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status)+"\n", dev));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtils.LogD(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                BluetoothGattCharacteristic alertLevel = null;
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                for (BluetoothGattService service : gatt.getServices()) {
                    StringBuilder allUUIDs = new StringBuilder("UUIDs={\nS=" + service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        allUUIDs.append(",\nC=").append(characteristic.getUuid());
                        if("00001113-0000-0000-8000-00801a2b3c4d".equals(characteristic.getUuid().toString())) {
                            alertLevel=characteristic;
                        }
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors())
                            allUUIDs.append(",\nD=").append(descriptor.getUuid());
                    }
                    allUUIDs.append("}");
                    LogUtils.LogD(TAG, "onServicesDiscovered:" + allUUIDs.toString());
                    //logTv("发现服务" + allUUIDs);
                }
                if(alertLevel != null){
                    LogUtils.LogD(TAG, "characteristic uuid: " + alertLevel.getUuid());
                    enableNotification(true,gatt,alertLevel);//必须要有，否则接收不到数据
                }
            }
        }
        private void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            if (gatt == null || characteristic == null)
                return; //这一步必须要有 否则收不到通知
            gatt.setCharacteristicNotification(characteristic, enable);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            LogUtils.LogD(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
//            logTv("读取C:" + valueStr);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            LogUtils.LogD(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
//            logTv("写入C:" + valueStr);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            LogUtils.LogD(TAG, String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr));
//            logTv("通知C:" + valueStr);
            if(uuid.toString().equals(UUID_CHAR_WRITE.toString())){
                LogUtils.LogD(TAG, "onCharacteristicChanged UUID_CHAR_WRITE");
                if(valueStr.length() > "BleServer#".length() && "BleServer#".equals(valueStr.substring(0, "BleServer#".length()))){
                    MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_MESSAGE_RECEIVE,
                            "BleServer: " + valueStr.substring(valueStr.indexOf("#")+1));
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            LogUtils.LogD(TAG,  String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            LogUtils.LogD(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            LogUtils.LogD(TAG, "onMtuChanged mtu:"+mtu);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices();
            }
        }
    };

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt){
        this.mBluetoothGatt = bluetoothGatt;
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    public boolean read() {
        BluetoothGattService service = getGattService(UUID_SERVICE);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_READ_NOTIFY);//通过UUID获取可读的Characteristic
            return mBluetoothGatt.readCharacteristic(characteristic);
        }
        return false;
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    public void write(String string) {
        BluetoothGattService service = getGattService(UUID_SERVICE);
        if (service != null) {
            //long time = System.currentTimeMillis();
            // String text = String.valueOf(time);
            LogUtils.LogD(TAG, "write string: " + string);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_WRITE);//通过UUID获取可写的Characteristic
            characteristic.setValue(string.getBytes()); //单次最多20个字节
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
    public void setNotify() {
        BluetoothGattService service = getGattService(UUID_SERVICE);
        if (service != null) {
            // 设置Characteristic通知
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_READ_NOTIFY);//通过UUID获取可通知的Characteristic
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESC_NOTITY);
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    // 获取Gatt服务
    private BluetoothGattService getGattService(UUID uuid) {
        if (!isConnected) {
            LogUtils.LogD(TAG," getGattService 没有连接");
            return null;
        }
        BluetoothGattService service = mBluetoothGatt.getService(uuid);
        if (service == null){
            LogUtils.LogD(TAG," getGattService 没有找到服务UUID = "+uuid);
        }
        return service;
    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    public void closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }
}
