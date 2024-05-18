package com.smart.access.control.services;

import static com.smart.access.control.services.BleAdapterService.SERVICE_UUID;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;


import java.util.*;

import androidx.core.app.ActivityCompat;

import com.smart.access.control.utils.Urls;

public class BleScanner {
    private BluetoothLeScanner scanner = null;
    private BluetoothAdapter bluetooth_adapter = null;
    private Handler handler = new Handler();
    private ScanResultsConsumer scan_results_consumer = null;
    private Context context = null;
    private boolean scanning = false;

    public BleScanner(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetooth_adapter = bluetoothManager.getAdapter();

        // check bluetooth is available and on
        if (bluetooth_adapter == null || !bluetooth_adapter.isEnabled()) {
            Log.d(Urls.TAG, "Bluetooth is NOT switched on");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return ;
            }
            context.startActivity(enableBtIntent);
        }
        Log.d(Urls.TAG, "Bluetooth is switched on");
    }


    public void startScanning(ScanResultsConsumer scan_results_consumer, long stop_after_ms) {
        try {
            if (scanning) {
                Log.d(Urls.TAG, "Already scanning so ignoring startScanning request");
                return;
            }
            Log.d(Urls.TAG, "Scanning...");
            if (scanner == null) {
                scanner = bluetooth_adapter.getBluetoothLeScanner();
                Log.d(Urls.TAG, "Created BluetoothScanner object");
            }


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (scanning) {
                        Log.d(Urls.TAG, "Stopping scanning");
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        scanner.stopScan(scan_callback);
                        setScanning(false);
                    }
                }
            },stop_after_ms);

            this.scan_results_consumer = scan_results_consumer;
            ArrayList<ScanFilter> filters = new ArrayList();
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SERVICE_UUID)).build();
            filters.add(filter);
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            setScanning(true);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            scanner.startScan(null, settings, scan_callback);
        } catch (Exception e) {
            Log.d("TAG", "startScanning: ${e.message}");
        }
    }


    public void stopScanning() {
        setScanning(false);
        Log.d(Urls.TAG, "Stopping scanning");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        scanner.stopScan(scan_callback);
    }

    private ScanCallback scan_callback = new  ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            try {
                if (!scanning) {
                    return;
                }
                scan_results_consumer.candidateBleDevice(
                        result.getDevice(),
                        result.getScanRecord().getBytes(),
                        result.getRssi()
                );
            } catch (Exception e) {
                Log.d("TAG", "onScanResult: ${e.message}");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(Urls.TAG, "onScanFailed");
        }
    };

    public boolean isScanning(){
        return scanning;
    }


    public void setScanning(boolean scanning) {
        try {
            this.scanning = scanning;
            if (!scanning) {
                scan_results_consumer.scanningStopped();
            } else {
                scan_results_consumer.scanningStarted();
            }
        } catch (Exception e) {
            Log.d("TAG", "setScanning: ${e.message}");
        }
    }

}
