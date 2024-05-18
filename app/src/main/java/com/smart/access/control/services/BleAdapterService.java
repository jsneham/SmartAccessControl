package com.smart.access.control.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


import androidx.core.app.ActivityCompat;

import com.smart.access.control.utils.Urls;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleAdapterService extends Service {

    private BluetoothAdapter bluetooth_adapter;
    private BluetoothGatt bluetooth_gatt;
    private BluetoothManager bluetooth_manager;
    private Handler activity_handler = null;
    private BluetoothDevice device;
    private BluetoothGattDescriptor descriptor;

    public BluetoothDevice getDevice() {
        return device;
    }


    // messages sent back to activity
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECT = 2;
    public static final int GATT_SERVICES_DISCOVERED = 3;
    public static final int GATT_CHARACTERISTIC_READ = 4;
    public static final int GATT_CHARACTERISTIC_WRITTEN = 5;
    public static final int GATT_REMOTE_RSSI = 6;
    public static final int MESSAGE = 7;
    public static final int NOTIFICATION_OR_INDICATION_RECEIVED = 8;

    public static final String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    // service characteristics
    public static final String CHARACTERISTIC_UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String CHARACTERISTIC_UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"; //"0000ffe2-0000-1000-8000-00805f9b34fb";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    // message parms
    public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
    public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
    public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
    public static final String PARCEL_VALUE = "VALUE";
    public static final String PARCEL_RSSI = "RSSI";
    public static final String PARCEL_TEXT = "TEXT";


//    public static String CLIENT_CHARACTERISTIC_CONFIG = "2902";

    private boolean request_processor_running = false;
    private boolean connected = false;



    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        // Method to retrieve the service instance
        public BleAdapterService getService() {
            return BleAdapterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    public boolean isConnected() {
        return connected;
    }

    // Ble Gatt Callback ///////////////////////
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.d("onConnectionStateChange: status=", String.valueOf(status));
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connected = true;
                Log.d("onConnectionStateChange: CONNECTED", "CONNECTED");
                Message msg = Message.obtain(activity_handler, GATT_CONNECTED);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(Urls.TAG, "onConnectionStateChange: DISCONNECTED");
                Message msg = Message.obtain(activity_handler, GATT_DISCONNECT);
                msg.sendToTarget();
                if (bluetooth_gatt != null) {
                    Log.d(Urls.TAG, "Closing and destroying BluetoothGatt object");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    connected = false;
                    bluetooth_gatt.close();
                    bluetooth_gatt = null;
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            sendConsoleMessage("Services Discovered");
//            Message msg = Message.obtain(activity_handler, GATT_SERVICES_DISCOVERED);
//            msg.sendToTarget();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Iterate through services and characteristics
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        // Check if characteristic supports notifications or indications
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            gatt.setCharacteristicNotification(characteristic, true);
                            // Enable notification on the BLE device
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
//
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Urls.TAG, "onCharacteristicRead");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_READ);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic read err:" + status);
            }
            // whatever the outcome we need to clear the Operation object from the request queue now

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Urls.TAG, "onCharacteristicWrite");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic write err:" + status);
            }
            // whatever the outcome we need to clear the Operation object from the request queue now
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            // notifications and indications are both communicated from here in this way
            Message msg = Message.obtain(activity_handler, NOTIFICATION_OR_INDICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }


        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage("RSSI read OK");
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message.obtain(activity_handler, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("RSSI read err:" + status);
            }
        }
    };




    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {

        if (bluetooth_manager == null) {
            bluetooth_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetooth_manager == null) {
                return;
            }
        }

        bluetooth_adapter = bluetooth_manager.getAdapter();
        if (bluetooth_adapter == null) {
            return;
        }

    }

    // connect to the device
    public boolean connect(final String address) {

        if (bluetooth_adapter == null || address == null) {
            sendConsoleMessage("connect: bluetooth_adapter=null");
            return false;
        }

        device = bluetooth_adapter.getRemoteDevice(address);
        if (device == null) {
            sendConsoleMessage("connect: device=null");
            return false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        bluetooth_gatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }


    // disconnect from device
    public void disconnect() {
        sendConsoleMessage("disconnecting");
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("disconnect: bluetooth_adapter|bluetooth_gatt null");
            return;
        }

        if (bluetooth_gatt != null) {
            Log.d(Urls.TAG, "Disconnecting");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetooth_gatt.disconnect();
        }
    }

    public void discoverServices() {
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetooth_gatt.discoverServices();
    }

    // set activity the will receive the messages
    public void setActivityHandler(Handler handler) {
        activity_handler = handler;
    }

    public void removeActivityHandler() {
        activity_handler = null;
    }

    // return list of supported services
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetooth_gatt == null)
            return null;
        return bluetooth_gatt.getServices();
    }

    public boolean readCharacteristic(String serviceUuid, String characteristicUuid) {

        Log.d(Urls.TAG, "readCharacteristic:$characteristicUuid of $serviceUuid");
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("readCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }
        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("readCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("readCharacteristic: gattChar null");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return bluetooth_gatt.readCharacteristic(gattChar);

    }


    public boolean writeCharacteristic(String serviceUuid, String characteristicUuid, byte[] value) {

        try {
            Log.d(Urls.TAG, "writeCharacteristic serviceUuid=" + serviceUuid + " characteristicUuid=" + characteristicUuid);
            if (bluetooth_adapter == null || bluetooth_gatt == null) {
                sendConsoleMessage("writeCharacteristic: bluetooth_adapter|bluetooth_gatt null");
                return false;
            }

            BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
            if (gattService == null) {
                sendConsoleMessage("writeCharacteristic: gattService null");
                return false;
            }
            BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
            if (gattChar == null) {
                sendConsoleMessage("writeCharacteristic: gattChar null");
                return false;
            }


            gattChar.setValue(value);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            return bluetooth_gatt.writeCharacteristic(gattChar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  false;
    }


    public boolean setIndicationsState(String serviceUuid, String characteristicUuid, boolean enabled) {

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("setIndicationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setIndicationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setIndicationsState: gattChar null");
            return false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        bluetooth_gatt.setCharacteristicNotification(gattChar, enabled);

        descriptor = gattChar.getDescriptor(java.util.UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return bluetooth_gatt.writeDescriptor(descriptor);
    }


    public void readRemoteRssi() {

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetooth_gatt.readRemoteRssi();
    }

    private void sendConsoleMessage(String text) {
        Message msg = Message.obtain(activity_handler, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }



}