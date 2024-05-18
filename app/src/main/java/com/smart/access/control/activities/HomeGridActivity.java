package com.smart.access.control.activities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smart.access.control.R;
import com.smart.access.control.adapters.BleListAdapter;
import com.smart.access.control.adapters.GridAdapter;
import com.smart.access.control.services.BleAdapterService;
import com.smart.access.control.services.BleScanner;
import com.smart.access.control.services.LocationService;
import com.smart.access.control.services.ScanResultsConsumer;
import com.smart.access.control.services.Utils;
import com.smart.access.control.utils.Urls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeGridActivity extends AppCompatActivity implements ScanResultsConsumer {

    RecyclerView recyclerView;
    GridAdapter gridAdapter;
    private ListView listView;
    private BleScanner bleScanner = null;
    private boolean permissionsGranted = false;
    private long scanTimeout = 5000;
    private int deviceCount = 0;
    private Toast toast;

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
    private BleAdapterService bluetoothLeAdapter;
    private String deviceName = null;
    private String deviceAddress = null;

    private BleListAdapter bleDeviceListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_grid);
        setRecyclerView();
        startServices();
        checkPermissions();
        initView();
    }

    private void initView() {
        bleDeviceListAdapter = new BleListAdapter(this);
        listView = findViewById(R.id.deviceList);
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
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

    private void setRecyclerView() {


        ArrayList<String> itemList = new ArrayList<>();
        itemList.add("User Management");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");

        recyclerView = findViewById(R.id.recyclerView);
        gridAdapter = new GridAdapter(this, itemList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(gridAdapter);

        // Set item click listener
        gridAdapter.setOnItemClickListener(new GridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position==0) {
                    openMasterKeyPopUp();
                }
            }
        });
    }


    public void openMasterKeyPopUp() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popup_master_key, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        EditText etPassword= view.findViewById(R.id.etPassword);
        TextView tvByteArray= view.findViewById(R.id.byteArray);
        view.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                String password = Utils.stringToHex(etPassword.getText().toString());
                //                String masterCommand = "0x020x810xFD0x060xFF" + password + "0x080x090x100xXX0xXX0x0D";
                //                byte[] byteArray = Utils.hexToByteArray(masterCommand);
                //
                //                String bt=" ";
                //                for(int i=0;i<=byteArray.length; i++){
                //                    bt = bt+ byteArray[i];
                //                    tvByteArray.setText(bt);
                //                }
                //
                //                senMsgToBleDevice(byteArray);

                String password = Utils.stringToHex(etPassword.getText().toString());
                byte[] passwordByteArray = Utils.hexToByteArray(password);

                byte[] startCommand = {0x02, (byte) 0x81,(byte)  0xFD, 0x06, (byte) 0xFF};
                byte[] endCommand = {0x07, 0x08, 0x09, 0x10, (byte) 0xFF, (byte) 0xFF, 0x0D};


                // Calculate total length for the resulting byte array
                int totalLength = passwordByteArray.length + startCommand.length + endCommand.length;

                // Create a new byte array to hold the combined data
                byte[] combinedByteArray = new byte[totalLength];

                // Copy data into the combinedByteArray
                int index = 0;
                System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
                index += startCommand.length;
                System.arraycopy(passwordByteArray, 0, combinedByteArray, index, passwordByteArray.length);
                index += passwordByteArray.length;
                System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
                senMsgToBleDevice(combinedByteArray);
            }
        });

        dialog.show();
    }

    private void senMsgToBleDevice(byte[] byteArray) {
            if (bluetoothLeAdapter != null) {
            if (bluetoothLeAdapter.writeCharacteristic(
                    BleAdapterService.SERVICE_UUID,
                    BleAdapterService.CHARACTERISTIC_UUID_TX,
                    byteArray)) {
                showToast("value d sent", Toast.LENGTH_SHORT);
            } else {
                showToast("No value received by ble device", Toast.LENGTH_SHORT);
            }
        } else {
                showToast("Please connect to SAC first before write Characteristic", Toast.LENGTH_SHORT);
        }

    }


    private void startServices() {
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, serviceConnection, this.BIND_AUTO_CREATE);
        showToast("Ready to find BLE devices", Toast.LENGTH_SHORT);
        bleScanner = new BleScanner(this);
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
                    bluetoothLeAdapter.discoverServices();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    showToast("DISCONNECTED", Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    // validate services and if ok....
                    List<BluetoothGattService> slist = bluetoothLeAdapter.getSupportedGattServices();
                    if (slist != null) {
                        for (BluetoothGattService svc : slist) {
                            showToast("UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId(), Toast.LENGTH_SHORT);

                            if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.SERVICE_UUID)) {
                                showToast(" uuid found", Toast.LENGTH_SHORT);
                                continue;
                            }
                        }

                        //                        bluetoothLeAdapter.readCharacteristic(
                        //                                BleAdapterService.SERVICE_UUID,
                        //                                BleAdapterService.CHARACTERISTIC_UUID_RX
                        //
                        //                        );
                        //                        bluetoothLeAdapter.setIndicationsState(
                        //                                BleAdapterService.SERVICE_UUID,
                        //                                BleAdapterService.CHARACTERISTIC_UUID_RX,
                        //                                true
                        //
                        //                        );
                    }




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
                    bundle = msg.getData();
                    String passwordReply=  Utils.convertByteHex((byte[]) bundle.get("VALUE"));
                    checkPassword(passwordReply);
                    Log.d("TAG", "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase());

                    break;
            }
        }
    };

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
            unbindService(serviceConnection);
            bluetoothLeAdapter = null;
        }

        // Stop BLE scanning process
        if (bleScanner != null && bleScanner.isScanning()) {
            bleScanner.stopScanning();
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_PERMISSION_CODE);
    }

    private void startScanning() {
        if (permissionsGranted) {

            showToast(Urls.SCANNING, Toast.LENGTH_SHORT);
            bleScanner.startScanning(this, scanTimeout);
        } else {
            Log.i(Urls.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }

    private void checkService() {
        if (!Utils.isLocationEnabledOrNot(this)) {
            Utils.showAlertLocation(this, getString(R.string.gps_enable), getString(R.string.please_turn_on_gps), getString(R.string.ok));
        }
        startServiceAfterLocationEnabled();
    }

    private void startServiceAfterLocationEnabled() {
        mLocationService = new LocationService();
        mServiceIntent = new Intent(this, mLocationService.getClass());
        if (!Utils.isMyServiceRunning(mLocationService.getClass(), this)) {
            startService(mServiceIntent);
            showToast(getString(R.string.service_start_successfully), Toast.LENGTH_SHORT);
        } else {
            showToast(getString(R.string.service_already_running), Toast.LENGTH_SHORT);
        }
    }

    private void showToast(String message, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void onScan() {
        if (!bleScanner.isScanning()) {
            deviceCount = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("ZISA");
        builder.setMessage("Permissions denied! Please go to settings and allow");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
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
        runOnUiThread(() -> {
            if (device != null) {
            }
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

//    private void checkBluetoothConnection() {
//        if (bluetoothLeAdapter != null) {
//            BluetoothDevice device = bluetoothLeAdapter.getConnectedDevice(deviceAddress);
//            if (device == null) {
//                // The device is not connected, show the popup
//                showConnectionPopup();
//            }
//        }
//    }

    private void checkPassword(String passwordReply) {
        showToast(passwordReply, Toast.LENGTH_SHORT);

    }
}