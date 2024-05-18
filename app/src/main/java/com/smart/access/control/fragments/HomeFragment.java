package com.smart.access.control.fragments;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smart.access.control.R;
import com.smart.access.control.activities.HomeGridActivity;
import com.smart.access.control.activities.UserOperationActivity;
import com.smart.access.control.adapters.BleListAdapter;
import com.smart.access.control.adapters.GridAdapter;
import com.smart.access.control.constants.ReplyCode;
import com.smart.access.control.services.BleAdapterService;
import com.smart.access.control.services.BleScanner;
import com.smart.access.control.services.LocationService;
import com.smart.access.control.services.ScanResultsConsumer;
import com.smart.access.control.services.Utils;
import com.smart.access.control.utils.Urls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements ScanResultsConsumer {

    private View view;
    private Context context;
    private Toast toast;
    private ListView listView;
    private ListView unpairedDeviceList;
    private LocationService mLocationService;

    private static final int REQUEST_PERMISSION_CODE = 123;
    private final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH
    };
    private boolean ble_scanning = false;
    private BleListAdapter bleDeviceListAdapter;
    private BleListAdapter unpairedBluetoothAdapter;
    private BleAdapterService bluetoothLeAdapter;

    private String deviceName = null;
    private String deviceAddress = null;

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothDevice device;

    private BleScanner bleScanner = null;
    private boolean permissionsGranted = false;
    private long scanTimeout = 5000;
    private int deviceCount = 0;
    private Intent mServiceIntent;

    RecyclerView recyclerView;
    GridAdapter gridAdapter;
    //    private static final long DELAY_TIME_MS = 10 * 60 * 1000; //10 Minute
    private static final long DELAY_TIME_MS = 10 * 1000; //10 Second


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();
        startServices();
        init();
        checkPermissions();
        setRecyclerView(view);
        return view;
    }

    private void setRecyclerView(View view) {

        ArrayList<String> itemList = new ArrayList<>();
        itemList.add("User Management");
        itemList.add("Enable Temp Access  (1min)");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");
        itemList.add("Dummy");

        recyclerView = view.findViewById(R.id.recyclerView);
        gridAdapter = new GridAdapter(context, itemList);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(gridAdapter);

        // Set item click listener
        gridAdapter.setOnItemClickListener(new GridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                switch (position) {
                    case 0:
                        if (bluetoothLeAdapter.isConnected()) {
                            openMasterKeyPopUp();
                        } else {
                            showToast("Device is not connected,Please connect to ABMT", Toast.LENGTH_SHORT);
                        }
                        break;
                    case 1:
                        sendEnableTempAccess();
                        break;

                }


            }
        });
    }

    private void sendEnableTempAccess() {
    }

    private void init() {

        unpairedBluetoothAdapter = new BleListAdapter(context);

//        listView = view.findViewById(R.id.deviceList);
//        listView.setAdapter(bleDeviceListAdapter);

        unpairedDeviceList = view.findViewById(R.id.unpairedDeviceList);
        unpairedDeviceList.setAdapter(unpairedBluetoothAdapter);

        unpairedDeviceList.setOnItemClickListener((parent, view, position, id) -> {

            if (ble_scanning) {
                bleScanner.stopScanning();
            }

            device = unpairedBluetoothAdapter.getDevice(position);
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

        });

//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if (bluetoothAdapter == null) {
//            // Device does not support Bluetooth
//            showToast("Bluetooth is not supported on this device", Toast.LENGTH_SHORT);
//        } else {
//            // Bluetooth is supported
//            if (!bluetoothAdapter.isEnabled()) {
//                // Bluetooth is not enabled, prompt user to enable it
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            } else {
//                // Bluetooth is enabled, show paired and unpaired devices
//                showPairedDevices();
////                showUnpairedDevices();
//            }
//        }
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

    private void showUnpairedDevices() {
        // Check if location permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasRequiredPermissions()) {
                checkService();
                if (bleScanner.isScanning()) {
                    startScanning();
                } else {
                    onScan();
                }
//                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//                context.registerReceiver(receiver, filter);
//                bluetoothAdapter.startDiscovery();
            } else {
                requestPermissions();
            }
        }
    }


    private void showPairedDevices() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bleDeviceListAdapter.addDevice(device);
            }
        } else {
//            bleDeviceListAdapter.add("No paired devices found");
        }
    }


    private void onConnect() {
        showToast("onConnect", Toast.LENGTH_SHORT);
        if (bluetoothLeAdapter != null) {
            if (bluetoothLeAdapter.connect(deviceAddress)) {
                showToast("onConnect: connect", Toast.LENGTH_SHORT);
            } else {
                showToast("onConnect: failed to connect", Toast.LENGTH_SHORT);
            }
        } else {
            showToast("onConnect: bluetooth_le_adapter=null", Toast.LENGTH_SHORT);
        }
    }


    private void openGridActivity() {

        if (isDeviceConnected(device)) {
            Intent in = new Intent(context, HomeGridActivity.class);
            startActivity(in);
        } else {
            showToast("Connect Your device to ", Toast.LENGTH_SHORT);
        }
    }

    private boolean isDeviceConnected(BluetoothDevice device) {
        if (device == null) {
            return false;
        }

        // Get the list of paired device names
        Set<String> pairedDeviceNames = getPairedDeviceNames();

        // Check if the given device name is in the list of paired device names
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return pairedDeviceNames.contains("ABMT");
    }

    private Set<String> getPairedDeviceNames() {
        Set<String> pairedDeviceNames = new HashSet<>();

        // Get the list of bonded devices
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                pairedDeviceNames.add(device.getName());
            }
        } else {
            // Handle permission denial
        }

        return pairedDeviceNames;
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


    private void showToast(String message, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Arrays.stream(grantResults).allMatch(result -> result == PackageManager.PERMISSION_GRANTED)) {
                    showToast("Permissions granted.", Toast.LENGTH_SHORT);
//                    showPairedDevices();
                    checkPermissions();
                } else {
                    openPopupForSettingScreen();
                }
            }
        }
    }

    private void openPopupForSettingScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.app_name));
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




    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            bluetoothLeAdapter = ((BleAdapterService.LocalBinder) service).getService();
//            bluetoothLeAdapter.setActivityHandler(messageHandler);

            BleAdapterService.LocalBinder binder = (BleAdapterService.LocalBinder) service;
            bluetoothLeAdapter = binder.getService();
            bluetoothLeAdapter.setActivityHandler(messageHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeAdapter = null;
        }
    };

    private void startServices() {
        Intent gattServiceIntent = new Intent(context, BleAdapterService.class);
        context.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        showToast("Ready to find BLE devices", Toast.LENGTH_SHORT);
        bleScanner = new BleScanner(context);
    }





    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (device != null && device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    unpairedBluetoothAdapter.addDevice(device);
                }
            }
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
                    Log.d("TAG", "NOTIFICATION_OR_INDICATION_RECEIVED: " + (byte[]) bundle.get("VALUE"));
                    String[] passwordReply = Utils.convertByteHexArray((byte[]) bundle.get("VALUE"));
                    checkResponse(passwordReply);
                    Log.d("TAG", "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase());

                    break;

            }
        }
    };

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

    private void startScanning() {
        if (permissionsGranted) {
            getActivity().runOnUiThread(() -> {
                unpairedBluetoothAdapter.clear();
                unpairedBluetoothAdapter.notifyDataSetChanged();
            });
            showToast(Urls.SCANNING, Toast.LENGTH_SHORT);
            bleScanner.startScanning(this, scanTimeout);
        } else {
            Log.i(Urls.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }

    @Override
    public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi) {
        getActivity().runOnUiThread(() -> {
            if (device != null) {
                unpairedBluetoothAdapter.addDevice(device);
            }
            unpairedBluetoothAdapter.notifyDataSetChanged();
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


    public void openMasterKeyPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.popup_master_key, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        EditText etPassword = view.findViewById(R.id.etPassword);
        view.findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(etPassword.getText().toString().length()<6){
                    etPassword.setError("Please enter valid Password");
                    return;
                }
                String password = Utils.stringToHex(etPassword.getText().toString());
                byte[] passwordByteArray = Utils.hexToByteArray(password);

                byte[] startCommand = {0x02, (byte) 0x81, (byte) 0xFD, 0x06, (byte) 0xFF};
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
                dialog.dismiss();
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

            } else {
                showToast("No value received by ble device", Toast.LENGTH_SHORT);
            }
        } else {
            showToast("Please connect to SAC first before write Characteristic", Toast.LENGTH_SHORT);
        }

    }

    private void checkResponse(String[] passwordReply) {

        if (passwordReply.length == 18) {
            String replyCode = passwordReply[4];
            switch (replyCode) {
                case ReplyCode.PASSWORD_MATCH:
                    showToast("Password match", Toast.LENGTH_SHORT);
                    showPopup();
                    break;
                case ReplyCode.PASSWORD_WRONG:
                    showToast("Password wrong", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.SYSTEM_BUSY_PASSWORD:
                    showToast("System Busy", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.OPERATION_NOT_ALLOWED:
                    showToast("OPERATION_NOT_ALLOWED", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_REGISTRATION_SUCCESSFUL:
                    showToast("USER_REGISTRATION_SUCCESSFUL", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_REGISTRATION_FAILED:
                    showToast("USER_REGISTRATION_FAILED", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_ALREADY_EXIST:
                    showToast("USER_ALREADY_EXIST", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_REGISTRATION_IN_PROCESS:
                    showToast("USER_REGISTRATION_IN_PROCESS", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.REMOVE_FINGER:
                    showToast("REMOVE_FINGER", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.PLACE_FINGER:
                    showToast("PLACE_FINGER", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.PLACE_SAME_FINGER_AGAIN:
                    showToast("PLACE_SAME_FINGER_AGAIN", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_REGISTRATION_TIMEOUT:
                    showToast("USER_REGISTRATION_TIMEOUT", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_FINGER_IMAGE_TOO_NOISY:
                    showToast("USER_FINGER_IMAGE_TOO_NOISY", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_FINGER_SENSOR_ISSUE:
                    showToast("USER_FINGER_SENSOR_ISSUE", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_FINGER_NOT_PLACED_PROPERLY:
                    showToast("USER_FINGER_NOT_PLACED_PROPERLY", Toast.LENGTH_SHORT);
                    break;
                default:
                    showToast("Some Error", Toast.LENGTH_SHORT);
                    break;
            }
        }

    }

    private void showPopup() {
        context.startActivity(new Intent(context, UserOperationActivity.class));
//        Handler handler = new Handler();
//
//        // Post a delayed action to show the popup after 10 minutes
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Call a method to show the popup
//                showUserOperationPopUp();
//            }
//        }, DELAY_TIME_MS);
    }



//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (bluetoothAdapter != null) {
//            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            bluetoothAdapter.cancelDiscovery();
//
//        }
//
////        if (isMyServiceRunning(BleAdapterService.class)) {
////            bluetoothLeAdapter.stopService(new Intent(context, BleAdapterService.class));
////        }
//
//        if (bluetoothLeAdapter != null) {
//            bluetoothLeAdapter.removeActivityHandler();
//            context.unbindService(serviceConnection);
//            bluetoothLeAdapter = null;
//        }
//
//    }


    @Override
    public void onStart() {
        super.onStart();
        // Re-attach the messageHandler when the fragment starts
        if (bluetoothLeAdapter != null) {
            bluetoothLeAdapter.removeActivityHandler();
            bluetoothLeAdapter.setActivityHandler(messageHandler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if the BleAdapterService is running and bind to it if necessary
        if (!isMyServiceRunning(BleAdapterService.class)) {
            startServices();
        }
        else{
            if (bluetoothLeAdapter != null) {
                bluetoothLeAdapter.removeActivityHandler();
                bluetoothLeAdapter.setActivityHandler(messageHandler);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the BleAdapterService and stop services when the fragment is destroyed
        stopServices();
    }

    private void stopServices() {
        // Unbind from BleAdapterService
        messageHandler.removeCallbacksAndMessages(null);
        if (bluetoothLeAdapter != null) {
            context.unbindService(serviceConnection);
            bluetoothLeAdapter = null;
        }

        // Stop BLE scanning process
        if (bleScanner != null && bleScanner.isScanning()) {
            bleScanner.stopScanning();
        }
    }





    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
