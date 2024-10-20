package com.smart.access.control.fragments;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ActionTypes;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemChangeListener;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.interfaces.TouchListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.smart.access.control.R;
import com.smart.access.control.activities.HomeGridActivity;
import com.smart.access.control.activities.UserOperationActivity;
import com.smart.access.control.adapters.BleListAdapter;
import com.smart.access.control.adapters.GridAdapter;
import com.smart.access.control.adapters.SliderAdapter;
import com.smart.access.control.constants.ReplyCode;
import com.smart.access.control.modals.GridItem;
import com.smart.access.control.services.BleAdapterService;
import com.smart.access.control.services.BleScanner;
import com.smart.access.control.services.LocationService;
import com.smart.access.control.services.ScanResultsConsumer;
import com.smart.access.control.services.Utils;
import com.smart.access.control.utils.Urls;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements ScanResultsConsumer {


    private View view;
    private Context context;
    private Toast toast;
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
    private ArrayList<BluetoothDevice> unpairedBluetoothDeviceList= new ArrayList<>();
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
    private boolean isBLEDeviceConnected = false;
    private ProgressDialog progressDialog;
    private ArrayList<String> deviceList = new ArrayList<>();
    private LinearLayout indicatorLayout ;
    private ViewPager viewPager;

    private long timeCountInMilliSeconds = 10 * 60000;
    private CountDownTimer countDownTimer;
    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private AlertDialog alertDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();
        startServices();
        init();
        checkPermissions();
        setRecyclerView(view);
        onSliderCall();
        return view;
    }

    @NonNull
    private static ArrayList<String> getDeviceListArrayList() {
        ArrayList<String> itemList = new ArrayList<String>();

        itemList.add("FC:B4:67:E0:B8:FA");
        itemList.add("EO:65:B8:14:A0:4E");
        itemList.add("30:C9:22:D8:5E:7A");
        itemList.add("30:C9:22:16:D7:6E");
        itemList.add("A4:CF:12:43:5A:62");
        itemList.add("FC:B4:67:E1:3A:5A");
        itemList.add("FC:B4:67:E0:14:9A");
        itemList.add("FC:B4:67:E1:07:AA");
        itemList.add("FC:B4:67:E0:13:DE");
        itemList.add("FC:B4:67:E0:E2:26");
        itemList.add("30:C9:22:D8:5C:B2");
        itemList.add("FC:B4:67:E1:39:DA");
        itemList.add("FC:B4:67:E0:CE:6A");
        itemList.add("FC:B4:67:E7:B9:76");
        itemList.add("FC:B4:67:E0:F1:22");
        itemList.add("FC:B4:67:E0:BB:CA");
        itemList.add("30:C9:22:16:DD:76");
        itemList.add("FC:B4:67:E7:CC:7E");
        itemList.add("FC:B4:67:E0:DC:B6");
        itemList.add("FC:B4:67:E0:63:22");
        itemList.add("C8:F0:9E:30:13:32");
        itemList.add("FC:B4:67:E1:FA:52");
        itemList.add("E0:65:B8:14:A0:4E");
        itemList.add("30:C9:22:D8:5C:82");
        itemList.add("E4:65:B8:14:A0:4E");
        itemList.add("FC:B4:67:E1:06:AA");



        return itemList;
    }

    private void setRecyclerView(View view) {
        deviceList = getDeviceListArrayList();

        ArrayList<GridItem> itemList = new ArrayList<>();
        itemList.add(new GridItem("User Management", R.drawable.user_management));
        itemList.add(new GridItem("Enable Temp Access  (1min)", R.drawable.temp_access));

        recyclerView = view.findViewById(R.id.recyclerView);
        gridAdapter = new GridAdapter(context, itemList);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(gridAdapter);

        // Set item click listener
        gridAdapter.setOnItemClickListener(position -> {
            if (bluetoothLeAdapter.isConnected() && isBLEDeviceConnected) {
                switch (position) {
                    case 0:

                        openMasterKeyPopUp();

                        break;
                    case 1:
                        sendEnableTempAccess();
                        break;

                }
            } else {
                if (deviceName == null) {
                    showToast("Device is not connected.", Toast.LENGTH_SHORT);
                } else
                    showToast("Device is not connected,Please connect to " + deviceName, Toast.LENGTH_SHORT);
            }


        });


    }

    private void sendEnableTempAccess() {
        byte[] randomTenDigitArray = {0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, (byte) 0xFF};

        byte[] startCommand = {0x02, (byte) 0x87, (byte) 0xFD, 0x00, (byte) 0xFF};
        byte[] endCommand = {(byte) 0xFF, (byte) 0xFF, 0x0D};

        int totalLength = randomTenDigitArray.length + startCommand.length + endCommand.length;

        // Create a new byte array to hold the combined data
        byte[] combinedByteArray = new byte[totalLength];

        // Copy data into the combinedByteArray
        int index = 0;
        System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
        index += startCommand.length;
        System.arraycopy(randomTenDigitArray, 0, combinedByteArray, index, randomTenDigitArray.length);
        index += randomTenDigitArray.length;
        System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
        senMsgToBleDevice(combinedByteArray);
    }

    private void init() {
        unpairedBluetoothDeviceList =new ArrayList<>();
    }

    private void onSliderCall() {


        ImageSlider imageSlider= view.findViewById(R.id.imageSlider);

        ArrayList<SlideModel> imageList = new ArrayList<>(); // Create image list

            imageList.add(new SlideModel(R.drawable.slider_one, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_two, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_three, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_four, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_five, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_six, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_seven, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_eight, ScaleTypes.FIT));
            imageList.add(new SlideModel(R.drawable.slider_nine, ScaleTypes.FIT));


        imageSlider.setImageList(imageList, ScaleTypes.FIT);


        imageSlider.setItemChangeListener(new ItemChangeListener() {
            @Override
            public void onItemChanged(int position) {
                // System.out.println("Pos: " + position);
            }
        });

        imageSlider.setTouchListener((touched, position) -> {
            if (touched == ActionTypes.DOWN) {
                imageSlider.stopSliding();
            } else if (touched == ActionTypes.UP) {
                imageSlider.startSliding(1000);
            }
        });
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



    private void onConnect() {
//        showToast("onConnect", Toast.LENGTH_SHORT);
        if (bluetoothLeAdapter != null) {
            if (bluetoothLeAdapter.connect(deviceAddress)) {
//                showToast("onConnect: connect", Toast.LENGTH_SHORT);
            } else {
                isBLEDeviceConnected = false;
                getActivity().invalidateOptionsMenu();
//                showToast("onConnect: failed to connect", Toast.LENGTH_SHORT);
            }
        } else {
            isBLEDeviceConnected = false;
            getActivity().invalidateOptionsMenu();
//            showToast("onConnect: bluetooth_le_adapter=null", Toast.LENGTH_SHORT);
        }

//        startTimerForPopup();
    }

    private void startTimerForPopup(String Messages) {
        // Create a Handler to run the code after 10 seconds
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage(Messages + deviceName + " ...");
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
//        progressDialog.show();


        int time = 0;
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_custom_timer, null);

        // Initialize the views in the custom layout
        progressBarCircle = customView.findViewById(R.id.progressBarCircle);
        textViewTime = customView.findViewById(R.id.textViewTime);
        TextView tvMessage = customView.findViewById(R.id.tvMessage);
        tvMessage.setText("Waiting for the connection");

        // Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(customView);

        // Create the AlertDialog
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Show the dialog
        alertDialog.show();

        // Initialize the progress bar values
        setProgressBarValues();
        // Start the countdown timer
        startCountDownTimer();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                progressDialog.dismiss();
                alertDialog.dismiss();
                if (isBLEDeviceConnected) {
                    showToast("Connected to " + deviceName, Toast.LENGTH_SHORT);
                } else {
                    showToast("Device is not connected,Please connect to " + deviceName, Toast.LENGTH_SHORT);
                }

            }
        }, 20000); // 10 seconds delay
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
//                    showToast("Permissions granted.", Toast.LENGTH_SHORT);
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
//        showToast("Ready to find BLE devices", Toast.LENGTH_SHORT);
        bleScanner = new BleScanner(context);
    }




    private final Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
//                    showToast(bundle.getString(BleAdapterService.PARCEL_TEXT), Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_CONNECTED:
                    startTimerForPopup("connecting to ");
                    showToast("Device is Connected", Toast.LENGTH_SHORT);
                    bluetoothLeAdapter.discoverServices();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();

                    }
                    if (bluetoothLeAdapter.isConnected()) {
                        bluetoothLeAdapter.disconnect();
                    }
                    showToast("Device is disconnected,Please connect to " + deviceName, Toast.LENGTH_SHORT);
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    // validate services and if ok....
                    List<BluetoothGattService> slist = bluetoothLeAdapter.getSupportedGattServices();
                    if (slist != null) {
                        for (BluetoothGattService svc : slist) {
//                            showToast("UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId(), Toast.LENGTH_SHORT);

                            if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.SERVICE_UUID)) {
//                                showToast(" uuid found", Toast.LENGTH_SHORT);
                                continue;
                            }
                        }
                    }


                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
                    bundle = msg.getData();
                    Log.d("TAG", "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase());
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    bundle = msg.getData();
                    Log.d("TAG", "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase());
                    break;
                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    Log.d("TAG", "NOTIFICATION_OR_INDICATION_RECEIVED: " + (byte[]) bundle.get("VALUE"));
                    byte[] response = (byte[]) bundle.get("VALUE");
                    String authentication = new String(response);
                    if (authentication.equals("AuthenticationDone")) {
                        isBLEDeviceConnected = true;
                        getActivity().invalidateOptionsMenu();
                    } else {
                        String[] passwordReply = Utils.convertByteHexArray(response);
                        checkResponse(passwordReply);
                    }


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
//            showToast(getString(R.string.service_start_successfully), Toast.LENGTH_SHORT);
        } else {
//            showToast(getString(R.string.service_already_running), Toast.LENGTH_SHORT);
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
//                unpairedBluetoothAdapter.clear();
//                unpairedBluetoothAdapter.notifyDataSetChanged();
                if(unpairedBluetoothDeviceList.size()>0)unpairedBluetoothDeviceList.clear();
            });
//            showToast(Urls.SCANNING, Toast.LENGTH_SHORT);
            bleScanner.startScanning(this, scanTimeout);
        } else {
            Log.i(Urls.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }

    @Override
    public void candidateBleDevice(BluetoothDevice device, byte[] scan_record, int rssi) {
        getActivity().runOnUiThread(() -> {
            if (device != null) {
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
                boolean isPresent = deviceList.contains(device.getAddress());
                if(device.getName().startsWith("*") && device.getName().endsWith("*") && isPresent){
//                    unpairedBluetoothAdapter.addDevice(device);
                    if (!unpairedBluetoothDeviceList.contains(device)) {
                        unpairedBluetoothDeviceList.add(device);
                    }

                }
            }
//            unpairedBluetoothAdapter.notifyDataSetChanged();
            deviceCount++;
        });
    }

    @Override
    public void scanningStarted() {
        showProgressDialog();
        setScanState(true);
    }

    @Override
    public void scanningStopped() {
        if (toast != null) {
            toast.cancel();
        }
        hideProgressDialog();
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


                if (etPassword.getText().toString().length() < 6) {
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
            showToast("Please connect to your device first before write Characteristic", Toast.LENGTH_SHORT);
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
                case ReplyCode.SYSTEM_BUSY:
                    showToast("System Busy", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.OPERATION_NOT_ALLOWED:
                    showToast("OPERATION_NOT_ALLOWED", Toast.LENGTH_SHORT);
                    break;

                case ReplyCode.TEMP_ACCESS_GRANTED:
                    showToast("TEMP_ACCESS_GRANTED", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.TEMP_ACCESS_DECLINE:
                    showToast("TEMP_ACCESS_DECLINE", Toast.LENGTH_SHORT);
                    break;

                default:
                    showToast("Some Error", Toast.LENGTH_SHORT);
                    break;
            }
        }

    }

    private void showPopup() {
        context.startActivity(new Intent(context, UserOperationActivity.class));
    }




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
        } else {
            if (bluetoothLeAdapter != null) {
                bluetoothLeAdapter.removeActivityHandler();
                bluetoothLeAdapter.setActivityHandler(messageHandler);
            }
        }

        handleBackPress();
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


//        getActivity().getApplicationContext().unregisterReceiver(mPairingRequestReceiver);
//        getActivity().getApplicationContext().unregisterReceiver(mPairingResultReceiver);

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_menu, menu);
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem settingsItem = menu.findItem(R.id.action_on_off);

        if (isBLEDeviceConnected) {
            settingsItem.setIcon(R.drawable.on); // Update the icon
            settingsItem.setTitle("Connected"); // Update the text
        } else {
            settingsItem.setIcon(R.drawable.off);
            settingsItem.setTitle("Disconnected");
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ble:
//                Toast.makeText(getActivity(), "connection clicked", Toast.LENGTH_SHORT).show();
                showDeviceList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeviceList() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.list_popup, null);
            builder.setView(dialogView);
            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            unpairedBluetoothAdapter = new BleListAdapter(context, unpairedBluetoothDeviceList);
            unpairedDeviceList = dialogView.findViewById(R.id.unpairedDeviceList);
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
                dialog.dismiss();

            });
            dialog.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private void showProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Bluetooth Scan Started");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }


    /**
     * Method to start the countdown timer
     */
    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                textViewTime.setText(hmsTimeFormatter(0));
                alertDialog.dismiss();
            }
        }.start();
    }

    /**
     * Method to set circular progress bar values
     */
    private void setProgressBarValues() {
        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    /**
     * Method to convert milliseconds to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    private void handleBackPress() {

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event here
                // For example, you can close the fragment or perform some action
                if (ble_scanning) {
                    bleScanner.stopScanning();
                }
                if (bluetoothLeAdapter.isConnected()) {
                    bluetoothLeAdapter.disconnect();
                }
                if(alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
            }
        });
    }

}
