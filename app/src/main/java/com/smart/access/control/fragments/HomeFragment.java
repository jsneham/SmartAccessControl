package com.smart.access.control.fragments;


import static android.content.Context.BIND_AUTO_CREATE;

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


    private  View view;
    private Button btnUnit;
    private Context context;
    private Toast toast;
    private ListView listView;

    private BleScanner bleScanner = null;
    private boolean permissionsGranted = false;
    private boolean ble_scanning = false;
    private long scanTimeout = 5000;
    private int deviceCount = 0;

    private static final int REQUEST_PERMISSION_CODE = 123;

    private final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    private LocationService mLocationService = new LocationService();
    private Intent mServiceIntent;

    private BleListAdapter bleDeviceListAdapter;
    
    


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_home, container, false);
        context =getContext();
        startServices();
        init();
        checkMulPermission();
        return  view;
    }

    private void startServices() {
        Intent gattServiceIntent = new Intent(context, BleAdapterService.class);
        context.bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        simpleToast("Ready to find BLE devices", 200);
        bleScanner = new BleScanner(context);
    }

    private BleAdapterService bluetoothLeAdapter;
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

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            String descriptor_uuid = "";
            byte[] b = null;


            switch (msg.what) {
                case BleAdapterService.MESSAGE: {
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    simpleToast(text, 2000);
                }
                break;
                case BleAdapterService.GATT_CONNECTED: {
                    // we're connected
                    simpleToast("CONNECTED",2000);

//                    bluetooth_le_adapter.discoverServices();
                }
                break;
                case BleAdapterService.GATT_DISCONNECT: {

                    // we're disconnected
                    simpleToast("DISCONNECTED",2000);


                }
                break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED: {

                    // validate services and if ok....


                }
                break;

                case BleAdapterService.GATT_CHARACTERISTIC_READ: {
                    bundle = msg.getData();
                    simpleToast(
                            "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID)
                                    .toUpperCase()
                                    + " Characteristic="
                                    + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID)
                                    .toUpperCase(),2000
                    );


                }
                break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN: {
                    bundle = msg.getData();
                    Log.d(
                            "TAG",
                            ("Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID)
                                    .toUpperCase()
                                    + " Characteristic="
                                    + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID)
                                    .toUpperCase())
                    );

                }
                break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED: {

                }
                break;
            }
        }
    };

    private void init() {
        btnUnit=view.findViewById(R.id.btnUnit);

        btnUnit.setOnClickListener(view -> {
            openGridActivity();
        });

        bleDeviceListAdapter = new BleListAdapter(context);


        listView = view.findViewById(R.id.deviceList);
        listView.setAdapter(bleDeviceListAdapter);
    }

    private void openGridActivity() {
        Intent in= new Intent(context, HomeGridActivity.class);
        startActivity(in);
    }



    // PERMISSION
    private void checkMulPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermissions();
            }
        } catch (Exception e) {
        }
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
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bleDeviceListAdapter.clear();
                    bleDeviceListAdapter.notifyDataSetChanged();
                }
            });
            simpleToast(Urls.SCANNING, 2000);
            bleScanner.startScanning(this, scanTimeout);
        } else {
            Log.i(Urls.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }
    private void checkService() {
        try {
            if (!Utils.isLocationEnabledOrNot(context)) {
                Utils.showAlertLocation(
                        context,
                        getString(R.string.gps_enable),
                        getString(R.string.please_turn_on_gps),
                        getString(R.string.ok)
                );
            }


            startServiceAfterLocationEnabled();
        } catch (Exception e) {
        }
    }
    private void startServiceAfterLocationEnabled() {
        try {
            mLocationService = new LocationService();
            mServiceIntent = new Intent(context, mLocationService.getClass());
            if (!Utils.isMyServiceRunning(mLocationService.getClass(), getActivity())) {
                context.startService(mServiceIntent);
                simpleToast(
                        getString(R.string.service_start_successfully), 2000
                );
            } else {
                simpleToast(
                        getString(R.string.service_already_running) ,2000
                );
            }
        } catch (Exception e) {
        }
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void onScan() {
        if (!bleScanner.isScanning()) {
//            Log.d(Constants.TAG, "Not currently scanning")
            deviceCount = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsGranted = false;
                    checkPermissions();
                } else {
                    Log.i(
                            Urls.TAG,
                            "Location permission has already been granted. Starting scanning."
                    );
                    permissionsGranted = true;
                }
            } else {
                // the ACCESS_COARSE_LOCATION permission did not exist before M so....
                permissionsGranted = true;
            }
            startScanning();
        } else {
            Log.d(Urls.TAG, "Already scanning");
            bleScanner.stopScanning();
        }
    }

    private void requestPermissionIfDenied() {
        try {// Show an alert dialog here with request explanation
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(
                    "Location, Camera, Read Contacts and Write External" +
                            " Storage permissions are required to do the task."
            );
            builder.setTitle("Please grant those permissions");
            builder.setPositiveButton("OK", (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                ActivityCompat.requestPermissions(
                        getActivity(), PERMISSIONS_LOCATION,
                        REQUEST_PERMISSION_CODE
                );

            });

            builder.setNeutralButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (grantResults.length > 0 && Arrays.stream(grantResults).allMatch(result -> result == PackageManager.PERMISSION_GRANTED)) {
                    permissionsGranted = true;
                    simpleToast("Permissions granted.", 2000);
                    checkPermissions();
                } else {
                    openPopupForSettingScreen();
                }
            }
        }
    }
    private void openPopupForSettingScreen() {

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            //set title for alert dialog
            builder.setTitle("ZISA");
            //set message for alert dialog
            builder.setMessage("Permissions denied! Please go to settings and allow");
//        builder.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);

            });

            // Create the AlertDialog
            AlertDialog alertDialog = builder.create();
            // Set other dialog properties
            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
        }
    }

    @Override
    public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi) {
        try {
            getActivity().runOnUiThread(() -> {
               if (device != null) {
                   bleDeviceListAdapter.addDevice(device);
               }
                bleDeviceListAdapter.notifyDataSetChanged();

                deviceCount++;
           });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
    }

    @Override
    public void scanningStopped() {
        toast.cancel();
        setScanState(false);
    }

    private void setScanState(boolean value) {
        ble_scanning = value;
        Log.d(Urls.TAG, "Setting scan state to $value");
//        (findViewById(R.id.scanButton) as Button).setText(if (value) Constants.STOP_SCANNING else Constants.FIND)
    }
}