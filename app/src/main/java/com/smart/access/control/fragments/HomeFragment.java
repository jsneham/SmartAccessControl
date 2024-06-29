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
import android.content.IntentFilter;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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
        return itemList;
    }

    private void setRecyclerView(View view) {
        deviceList = getDeviceListArrayList();

        ArrayList<GridItem> itemList = new ArrayList<>();
        itemList.add(new GridItem("User Management", R.drawable.user_management));
        itemList.add(new GridItem("Enable Temp Access  (1min)", R.drawable.temp_access));
//        itemList.add(new GridItem("Dummy", 3));
//        itemList.add(new GridItem("Dummy", 4));
//        itemList.add(new GridItem("Dummy", 2));
//        itemList.add(new GridItem("Dummy", 5));

        recyclerView = view.findViewById(R.id.recyclerView);
        gridAdapter = new GridAdapter(context, itemList);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(gridAdapter);

        // Set item click listener
        gridAdapter.setOnItemClickListener(new GridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
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

//        unpairedBluetoothAdapter = new BleListAdapter(context);
        unpairedBluetoothDeviceList =new ArrayList<>();


//        listView = view.findViewById(R.id.deviceList);
//        listView.setAdapter(bleDeviceListAdapter);

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

    private void onSliderCall() {
        ArrayList<Integer> sliders = new ArrayList<Integer>();

        sliders.add(R.drawable.slider_one);
        sliders.add(R.drawable.slider_two);
        viewPager= view.findViewById(R.id.viewPager);
        indicatorLayout = view.findViewById(R.id.indicatorLayout);


        Context context = getContext();
        if (context == null) {
            return;
        }
        SliderAdapter adapter = new SliderAdapter(context, sliders);
        if (viewPager != null) {
            viewPager.setAdapter(adapter);
            setupIndicator(sliders.size());
            int pageMargin = getResources().getDimensionPixelOffset(R.dimen.dp10);
            viewPager.setPageMargin(pageMargin);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageSelected(int position) {
                    updateIndicator(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {}
            });
        }
    }

    private void updateIndicator(int position) {
        int childCount = indicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) indicatorLayout.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.indicator_inactive));
            }
        }
    }


    private void setupIndicator(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(context);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            indicatorLayout.addView(indicators[i]);
        }

        // Make sure to only set the active indicator if count is greater than 0
        if (count > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active));
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
                isBLEDeviceConnected = false;
                getActivity().invalidateOptionsMenu();
                showToast("onConnect: failed to connect", Toast.LENGTH_SHORT);
            }
        } else {
            isBLEDeviceConnected = false;
            getActivity().invalidateOptionsMenu();
            showToast("onConnect: bluetooth_le_adapter=null", Toast.LENGTH_SHORT);
        }

//        startTimerForPopup();
    }

    private void startTimerForPopup(String Messages) {
        // Create a Handler to run the code after 10 seconds
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(Messages + deviceName + " ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if (isBLEDeviceConnected) {
                    showToast("Connected to " + deviceName, Toast.LENGTH_SHORT);
                } else {
                    showToast("Device is not connected,Please connect to " + deviceName, Toast.LENGTH_SHORT);
                }

            }
        }, 20000); // 10 seconds delay
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
        return pairedDeviceNames.contains("");
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
//                    unpairedBluetoothAdapter.addDevice(device);
                    unpairedBluetoothDeviceList.add(device);
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
//                unpairedBluetoothAdapter.clear();
//                unpairedBluetoothAdapter.notifyDataSetChanged();
                if(unpairedBluetoothDeviceList.size()>0)unpairedBluetoothDeviceList.clear();
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
        } else {
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


    private BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int pin = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, 0);
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
                device.createBond();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    startTimerForPopup("Is Pairing to ");
                }

            }
        }
    };


    private BroadcastReceiver mPairingResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    // Device successfully bonded
                    showToast("Device successfully bonded", Toast.LENGTH_SHORT);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else if (bondState == BluetoothDevice.BOND_NONE) {
                    // Bonding failed or removed
                    showToast("Bonding failed or removed", Toast.LENGTH_SHORT);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        }
    };
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
            case R.id.action_on_off:
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

}
