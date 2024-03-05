package com.smart.access.control.services;

import android.bluetooth.BluetoothDevice;

public interface ScanResultsConsumer {
    void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi);
    void scanningStarted();
    void scanningStopped();
}
