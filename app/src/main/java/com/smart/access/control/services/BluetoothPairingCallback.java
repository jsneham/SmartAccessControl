package com.smart.access.control.services;

import android.bluetooth.BluetoothDevice;

public interface BluetoothPairingCallback {
    void onPairingInitiated(BluetoothDevice device);
    void onPairingSuccess(BluetoothDevice device);
    void onPairingFailed(BluetoothDevice device, int errorCode);
}
