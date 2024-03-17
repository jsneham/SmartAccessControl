package com.smart.access.control.fragments;


// Add your package declaration here

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.smart.access.control.R;
import com.smart.access.control.activities.HomeGridActivity;
import com.smart.access.control.adapters.BleListAdapter;
import com.smart.access.control.services.BleAdapterService;
import com.smart.access.control.services.BleScanner;
import com.smart.access.control.services.LocationService;
import com.smart.access.control.services.ScanResultsConsumer;
import com.smart.access.control.services.Utils;
import com.smart.access.control.utils.Urls;

import java.util.Arrays;

public class HomeFragment extends Fragment implements ScanResultsConsumer {

    private View view;
    private Button btnUnit;
    private Context context;
    private Toast toast;
    private ListView listView;

    private BleScanner bleScanner = null;
    private boolean permissionsGranted = false;
    private long scanTimeout = 5000;
    private int deviceCount = 0;

    private static final int REQUEST_PERMISSION_CODE = 123;
    private final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private boolean ble_scanning = false;
    private LocationService mLocationService;
    private Intent mServiceIntent;
    private BleListAdapter bleDeviceListAdapter;
    private BleAdapterService bluetoothLeAdapter;
    private String deviceName = null;
    private String deviceAddress = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();
        startServices();
        init();
        checkPermissions();
        return view;
    }

    private void startServices() {
        Intent gattServiceIntent = new Intent(context, BleAdapterService.class);
        context.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        showToast("Ready to find BLE devices", Toast.LENGTH_SHORT);
        bleScanner = new BleScanner(context);
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeAdapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetoothLeAdapter.setActivityHandler(messageHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeAdapter = null;
        }
    };

    private final Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    showToast(bundle.getString(BleAdapterService.PARCEL_TEXT), Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_CONNECTED:
                    showToast("CONNECTED", Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    showToast("DISCONNECTED", Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    // validate services and if ok....
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    bundle = msg.getData();
                    showToast("Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase(), Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    bundle = msg.getData();
                    Log.d("TAG", "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase());
                    break;
                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    break;
            }
        }
    };

    private void init() {
        btnUnit = view.findViewById(R.id.btnUnit);
        btnUnit.setOnClickListener(view -> openGridActivity());

        bleDeviceListAdapter = new BleListAdapter(context);
        listView = view.findViewById(R.id.deviceList);
        listView.setAdapter(bleDeviceListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (ble_scanning) {
                    bleScanner.stopScanning();
                }

                BluetoothDevice device = bleDeviceListAdapter.getDevice(position);
                if (toast != null) {
                    toast.cancel();
                }
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                deviceName = device.getName();
                deviceAddress = device.getAddress();
                onConnect();

            }
        });
    }

    private void onConnect() {
        showToast("onConnect" , Toast.LENGTH_SHORT);
        if (bluetoothLeAdapter != null) {
            if (bluetoothLeAdapter.connect(deviceAddress)) {
                showToast("onConnect: Connect", Toast.LENGTH_SHORT);
            } else {
                showToast("onConnect: failed to connect", Toast.LENGTH_SHORT);
            }
        } else {
            showToast("onConnect: bluetooth_le_adapter=null", Toast.LENGTH_SHORT);
        }
    }

    private void stopServices() {
        // Unbind from BleAdapterService
        if (bluetoothLeAdapter != null) {
            context.unbindService(serviceConnection);
            bluetoothLeAdapter = null;
        }

        // Stop BLE scanning process
        if (bleScanner != null && bleScanner.isScanning()) {
            bleScanner.stopScanning();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServices();
//        Log.d("TAG", "onBackPressed");
//        if (bluetoothLeAdapter != null && bluetoothLeAdapter.isConnected()) {
//            try {
//                bluetoothLeAdapter.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//
//        }
    }




    private void openGridActivity() {
        Intent in = new Intent(context, HomeGridActivity.class);
        startActivity(in);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasRequiredPermissions()) {
                checkService();
                if (bleScanner.isScanning()) {
                    startScanning();
                } else {
                    onScan();
                }
            } else {
                requestPermissions();
            }
        }
    }

    private boolean hasRequiredPermissions() {
        for (String permission : PERMISSIONS_LOCATION) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_LOCATION, REQUEST_PERMISSION_CODE);
    }

    private void startScanning() {
        if (permissionsGranted) {
            getActivity().runOnUiThread(() -> {
                bleDeviceListAdapter.clear();
                bleDeviceListAdapter.notifyDataSetChanged();
            });
            showToast(Urls.SCANNING, Toast.LENGTH_SHORT);
            bleScanner.startScanning(this, scanTimeout);
        } else {
            Log.i(Urls.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }

    private void checkService() {
        if (!Utils.isLocationEnabledOrNot(context)) {
            Utils.showAlertLocation(context, getString(R.string.gps_enable), getString(R.string.please_turn_on_gps), getString(R.string.ok));
        }
        startServiceAfterLocationEnabled();
    }

    private void startServiceAfterLocationEnabled() {
        mLocationService = new LocationService();
        mServiceIntent = new Intent(context, mLocationService.getClass());
        if (!Utils.isMyServiceRunning(mLocationService.getClass(), getActivity())) {
            context.startService(mServiceIntent);
            showToast(getString(R.string.service_start_successfully), Toast.LENGTH_SHORT);
        } else {
            showToast(getString(R.string.service_already_running), Toast.LENGTH_SHORT);
        }
    }

    private void showToast(String message, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void onScan() {
        if (!bleScanner.isScanning()) {
            deviceCount = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false;
                    checkPermissions();
                } else {
                    Log.i(Urls.TAG, "Location permission has already been granted. Starting scanning.");
                    permissionsGranted = true;
                }
            } else {
                permissionsGranted = true;
            }
            startScanning();
        } else {
            Log.d(Urls.TAG, "Already scanning");
            bleScanner.stopScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Arrays.stream(grantResults).allMatch(result -> result == PackageManager.PERMISSION_GRANTED)) {
                    permissionsGranted = true;
                    showToast("Permissions granted.", Toast.LENGTH_SHORT);
                    checkPermissions();
                } else {
                    openPopupForSettingScreen();
                }
            }
        }
    }

    private void openPopupForSettingScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("ZISA");
        builder.setMessage("Permissions denied! Please go to settings and allow");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi) {
        getActivity().runOnUiThread(() -> {
            if (device != null) {
                bleDeviceListAdapter.addDevice(device);
            }
            bleDeviceListAdapter.notifyDataSetChanged();
            deviceCount++;
        });
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
    }

    @Override
    public void scanningStopped() {
        if (toast != null) {
            toast.cancel();
        }
        setScanState(false);
    }

    private void setScanState(boolean value) {
        ble_scanning = value;
        Log.d(Urls.TAG, "Setting scan state to " + value);
    }




}
