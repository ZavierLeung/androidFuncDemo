package com.zavier.androidfunctiontestv2.bluetoothTest.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;

import com.zavier.androidfunctiontestv2.customUtils.LogUtils;
import com.zavier.androidfunctiontestv2.customUtils.MessageUtils;

import java.util.Arrays;
import java.util.UUID;

public class BleServer {
    public static final UUID UUID_SERVICE = UUID.fromString("00001111-0000-0000-8000-00801A2B3C4D"); //自定义UUID
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("00001112-0000-0000-8000-00801A2B3C4D");
    public static final UUID UUID_CHAR_WRITE = UUID.fromString("00001113-0000-0000-8000-00801A2B3C4D");
    public static final UUID UUID_DESC_NOTITY = UUID.fromString("00001114-0000-0000-8000-00801A2B3C4D");
    private static final String TAG = "BleServer";
    private BluetoothGattServer mBluetoothGattServer; // BLE服务端
    private Context mContext;
    private Handler mHandler;
    private BluetoothDevice mBleDevice;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser; // BLE广播

    public BleServer(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
        initBleServer();
    }

    // BLE广播Callback
    public AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            LogUtils.LogD(TAG,"BLE广播开启成功");
        }

        @Override
        public void onStartFailure(int errorCode) {
            LogUtils.LogD(TAG,"BLE广播开启失败,错误码:" + errorCode);
        }
    };

    private void initBleServer(){
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // ============启动BLE蓝牙广播(广告) =================================================================================
        //广播设置(必须)
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //广播模式: 低功耗,平衡,低延迟
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //发射功率级别: 极低,低,中,高
                .setConnectable(true) //能否连接,广播分为可连接广播和不可连接广播
                .build();
        //广播数据(必须，广播启动就会发送)
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true) //包含蓝牙名称s
                .setIncludeTxPowerLevel(true) //包含发射功率级别
                .addManufacturerData(1, new byte[]{23, 33}) //设备厂商数据，自定义
                .build();
        //扫描响应数据(可选，当客户端扫描时才发送)
        AdvertiseData scanResponse = new AdvertiseData.Builder()
                .addManufacturerData(2, new byte[]{66, 66}) //设备厂商数据，自定义
                .addServiceUuid(new ParcelUuid(BleServer.UUID_SERVICE)) //服务UUID
//                .addServiceData(new ParcelUuid(UUID_SERVICE), new byte[]{2}) //服务数据，自定义
                .build();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponse, mAdvertiseCallback);

        // 注意：必须要开启可连接的BLE广播，其它设备才能发现并连接BLE服务端!
        // =============启动BLE蓝牙服务端=====================================================================================
        BluetoothGattService service = new BluetoothGattService(BleServer.UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //添加可读+通知characteristic
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(BleServer.UUID_CHAR_READ_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        characteristicRead.addDescriptor(new BluetoothGattDescriptor(BleServer.UUID_DESC_NOTITY, BluetoothGattCharacteristic.PERMISSION_WRITE));
        service.addCharacteristic(characteristicRead);
        //添加可写characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(BleServer.UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristicWrite);
        if (bluetoothManager != null){
            setBluetoothGattServer(bluetoothManager.openGattServer(mContext, mBluetoothGattServerCallback));
            getBluetoothGattServer().addService(service);
        }
    }

    public void setBluetoothGattServer(BluetoothGattServer bluetoothGattServer){
        this.mBluetoothGattServer = bluetoothGattServer;
    }

    public BluetoothGattServer getBluetoothGattServer(){
        return mBluetoothGattServer;
    }

    // BLE服务端Callback
    public BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            mBleDevice = device;
            LogUtils.LogD(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", device.getName(), device.getAddress(), status, newState));
            MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_CONNECT_SUCCESS, String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), device)+"\n");
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            LogUtils.LogD(TAG, String.format("onServiceAdded:%s,%s", status, service.getUuid()));
            MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_CONNECT_SERVER,
                    String.format(status == 0 ? "添加服务[%s]成功" : "添加服务[%s]失败,错误码:" + status, service.getUuid())+"\n");
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            LogUtils.LogD(TAG,  String.format("onCharacteristicReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, characteristic.getUuid()));
            String response = "C_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes());// 响应客户端
            LogUtils.LogD(TAG, "onCharacteristicReadRequest:" + response);
            MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_MESSAGE_SEND,response);
        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            // 获取客户端发过来的数据
            final String requestStr = new String(requestBytes);
            LogUtils.LogD(TAG,  String.format("onCharacteristicWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, characteristic.getUuid(),
                    preparedWrite, responseNeeded, offset, requestStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);// 响应客户端
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(1000);
                    characteristic.setValue(requestStr);
                    mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
                    LogUtils.LogD(TAG, "onCharacteristicWriteRequest:" + requestStr);
                    MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_MESSAGE_RECEIVE,"BleClient: " + requestStr);
                }
            }).start();
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            LogUtils.LogD(TAG,  String.format("onDescriptorReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, descriptor.getUuid()));
            String response = "D_" + (int) (Math.random() * 100); //模拟数据
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes()); // 响应客户端
            LogUtils.LogD(TAG, "onCharacteristicReadRequest:" + response);
//            mLogUtils.sendMessage(BluetoothSecondActivity.MESSAGE_SEND,response);
        }

        @Override
        public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            // 获取客户端发过来的数据
            final String valueStr = Arrays.toString(value);
            LogUtils.LogD(TAG,  String.format("onDescriptorWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, descriptor.getUuid(),
                    preparedWrite, responseNeeded, offset, valueStr));
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端

            // 简单模拟通知客户端Characteristic变化
            if (Arrays.toString(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE).equals(valueStr)) { //是否开启通知
                final BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        characteristic.setValue(valueStr);
                        mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
                        LogUtils.LogD(TAG, "onCharacteristicWriteRequest Characteristic:" + valueStr);
                        MessageUtils.sendMessage(mHandler, MessageUtils.MSG_BLUETOOTH_MESSAGE_RECEIVE,
                                "BleClient: "+valueStr);
                    }
                }).start();
            }
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            LogUtils.LogD(TAG,  String.format("onExecuteWrite:%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, execute));
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            LogUtils.LogD(TAG,  String.format("onNotificationSent:%s,%s,%s", device.getName(), device.getAddress(), status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            LogUtils.LogD(TAG,  String.format("onMtuChanged:%s,%s,%s", device.getName(), device.getAddress(), mtu));
        }
    };

    public void closeBleServer() {
        if (mBluetoothLeAdvertiser != null)
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);

        if (mBluetoothGattServer != null)
            mBluetoothGattServer.close();
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    public void write(String string) {
        BluetoothGattService service = mBluetoothGattServer.getService(UUID_SERVICE);
        if (service != null) {
            //long time = System.currentTimeMillis();
            // String text = String.valueOf(time);

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_WRITE);//通过UUID
            // 获取可写的Characteristic
            characteristic.setValue(string.getBytes()); //单次最多20个字节
            mBluetoothGattServer.notifyCharacteristicChanged(mBleDevice,characteristic,false);

        }
    }
}
