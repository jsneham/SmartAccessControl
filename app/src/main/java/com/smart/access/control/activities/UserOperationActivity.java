package com.smart.access.control.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.access.control.R;
import com.smart.access.control.adapters.GridAdapter;
import com.smart.access.control.adapters.UserListAdapter;
import com.smart.access.control.constants.ReplyCode;
import com.smart.access.control.services.BleAdapterService;
import com.smart.access.control.services.LocationService;
import com.smart.access.control.services.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserOperationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GridAdapter gridAdapter;
    private Toast toast;

    private BleAdapterService bluetoothLeAdapter;

    private ArrayList<UserData>  userDetailsList = new ArrayList();
    private static final int REQUEST_PERMISSION_CODE = 123;
    private final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH
    };

    BluetoothDevice device;
    private LocationService mLocationService;


    private boolean permissionsGranted = false;
    private long scanTimeout = 5000;
    private int deviceCount = 0;
    private Intent mServiceIntent;
    private boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_operation);
//        checkPermissions();
        initView();
        setRecyclerView();
//        startServices();

    }

    private void initView() {

    }

    private void setRecyclerView() {


        ArrayList<String> itemList = new ArrayList<>();
        itemList.add("Add User");
        itemList.add("Delete User");
        itemList.add("Delete All User");
        itemList.add("View All User");
        itemList.add("Read Specific User");

        recyclerView = findViewById(R.id.recyclerView);
        gridAdapter = new GridAdapter(this, itemList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(gridAdapter);

        // Set item click listener
        gridAdapter.setOnItemClickListener(new GridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:
                        openAddUserPopUp();
                        break;
                    case 1:
                        openDeleteUserPopUp();
                        break;
                    case 2:
                        openDeleteAllUserPopUp();
                        break;
                    case 3:
                        openViewAllUserPopUp();
                        break;

                    case 4:
                        openViewUserPopUp();
                        break;
                }

            }
        });
    }


    private void openViewAllUserPopUp() {
        byte[] userNameByteArray = {0x02,0x03,0x04,0x05,0x06,0x07, 0x08, 0x09, 0x10, (byte) 0xFF};

        byte[] startCommand = {0x02, (byte) 0x85, (byte) 0xFD, 0x00, (byte) 0xFF};
        byte[] endCommand = {(byte) 0xFF, (byte) 0xFF, 0x0D};

        // Calculate total length for the resulting byte array
        int totalLength = userNameByteArray.length + startCommand.length + endCommand.length;

        // Create a new byte array to hold the combined data
        byte[] combinedByteArray = new byte[totalLength];

        // Copy data into the combinedByteArray
        int index = 0;
        System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
        index += startCommand.length;
        System.arraycopy(userNameByteArray, 0, combinedByteArray, index, userNameByteArray.length);
        index += userNameByteArray.length;
        System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
        senMsgToBleDevice(combinedByteArray);

    }

    private void openViewUserPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_user_master_key, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        EditText etInput = view.findViewById(R.id.etInput);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String numberInput = etInput.getText().toString();

            if (Integer.parseInt(numberInput) > 500) {
                etInput.setError("Please Enter Valid number");
                return;
            }
            String number = Utils.stringToHex(numberInput);
            byte[] passwordByteArray = Utils.hexToByteArray(number);
            byte[] userNameByteArray = {0x04,0x05,0x06,0x07, 0x08, 0x09, 0x10, (byte) 0xFF};


            byte[] startCommand = {0x02, (byte) 0x84, (byte) 0xFD, 0x02, (byte) 0xFF};
            byte[] endCommand = {(byte) 0xFF, (byte) 0xFF, 0x0D};

            // Calculate total length for the resulting byte array
            int totalLength = passwordByteArray.length + userNameByteArray.length + startCommand.length + endCommand.length;

            // Create a new byte array to hold the combined data
            byte[] combinedByteArray = new byte[totalLength];

            // Copy data into the combinedByteArray
            int index = 0;
            System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
            index += startCommand.length;
            System.arraycopy(passwordByteArray, 0, combinedByteArray, index, passwordByteArray.length);
            index += passwordByteArray.length;
            System.arraycopy(userNameByteArray, 0, combinedByteArray, index, userNameByteArray.length);
            index += userNameByteArray.length;
            System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
            senMsgToBleDevice(combinedByteArray);
            dialog.dismiss();
        });

        dialog.show();

    }

    private void openDeleteAllUserPopUp() {
        byte[] userNameByteArray = {0x02,0x03,0x04,0x05,0x06,0x07, 0x08, 0x09, 0x10, (byte) 0xFF};

        byte[] startCommand = {0x02, (byte) 0x85, (byte) 0xFD, 0x00, (byte) 0xFF};
        byte[] endCommand = {(byte) 0xFF, (byte) 0xFF, 0x0D};

        // Calculate total length for the resulting byte array
        int totalLength = userNameByteArray.length + startCommand.length + endCommand.length;

        // Create a new byte array to hold the combined data
        byte[] combinedByteArray = new byte[totalLength];

        // Copy data into the combinedByteArray
        int index = 0;
        System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
        index += startCommand.length;
        System.arraycopy(userNameByteArray, 0, combinedByteArray, index, userNameByteArray.length);
        index += userNameByteArray.length;
        System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
        senMsgToBleDevice(combinedByteArray);

    }
    private void openDeleteUserPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_user_master_key, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        EditText etInput = view.findViewById(R.id.etInput);
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if the input is less than or equal to 500 characters
                if (s.length() <= 500) {
                    // Enable the submit button
                    checkbox.setVisibility(View.VISIBLE);
                    btnSubmit.setEnabled(true);
                } else {
                    // Disable the submit button
                    checkbox.setVisibility(View.GONE);
                    btnSubmit.setEnabled(false);
                }
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String numberInput = etInput.getText().toString();

            if(!checkbox.isChecked()){
                checkbox.setError("Please select this");
                showToast("Please Confirm by clicking on checkbox",Toast.LENGTH_SHORT);
                return;
            }
            if (Integer.parseInt(numberInput) > 500) {
                etInput.setError("Please Enter Valid number");
                return;
            }

            String number = Integer.toHexString(Integer.parseInt(numberInput));
            if (number.length() == 1) {
                number = "000" + number;
            } else if (number.length() == 2) {
                number = "00" + number;
            } else if (number.length() == 3) {
                number = "0" + number;
            }
            byte[] passwordByteArray = Utils.hexToByteArray(number);
            byte[] userNameByteArray = {0x04,0x05,0x06,0x07, 0x08, 0x09, 0x10, (byte) 0xFF};

            byte[] startCommand = {0x02, (byte) 0x83, (byte) 0xFD, 0x02, (byte) 0xFF};
            byte[] endCommand = {(byte) 0xFF, (byte) 0xFF, 0x0D};

            // Calculate total length for the resulting byte array
            int totalLength = passwordByteArray.length + userNameByteArray.length + startCommand.length + endCommand.length;

            // Create a new byte array to hold the combined data
            byte[] combinedByteArray = new byte[totalLength];

            // Copy data into the combinedByteArray
            int index = 0;
            System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
            index += startCommand.length;
            System.arraycopy(passwordByteArray, 0, combinedByteArray, index, passwordByteArray.length);
            index += passwordByteArray.length;
            System.arraycopy(userNameByteArray, 0, combinedByteArray, index, userNameByteArray.length);
            index += userNameByteArray.length;
            System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
            senMsgToBleDevice(combinedByteArray);
            dialog.dismiss();
        });

        dialog.show();

    }

    private void showUserOperationPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.user_operation_poup, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        Button btnAddUser = view.findViewById(R.id.btnAddUser);
        Button btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        Button btnDeleteAllUser = view.findViewById(R.id.btnDeleteAllUser);
        Button btnViewAllUser = view.findViewById(R.id.btnViewAllUser);

        btnAddUser.setOnClickListener(view1 -> {
            dialog.dismiss();
            openAddUserPopUp();
        });

        btnDeleteUser.setOnClickListener(view1 -> {

        });

        btnDeleteAllUser.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        btnViewAllUser.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        dialog.show();
    }


    private void openAddUserPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.add_user_master_key, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        EditText etInput = view.findViewById(R.id.etInput);
        EditText etUserName = view.findViewById(R.id.etUserName);
        TextView txtMsg = view.findViewById(R.id.txtMsg);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if the input is less than or equal to 500 characters
                if (s.length() <= 500) {
                    // Enable the submit button
//                    txtMsg.setVisibility(View.VISIBLE);
                    btnSubmit.setEnabled(true);
                } else {
                    // Disable the submit button
//                    txtMsg.setVisibility(View.GONE);
                    btnSubmit.setEnabled(false);
                }
            }
        });
        btnSubmit.setOnClickListener(v -> {
            String numberInput = etInput.getText().toString();
            String userNameInput = etUserName.getText().toString();
            if (Integer.parseInt(numberInput) > 500 || userNameInput.isEmpty() || userNameInput.length() < 8 || userNameInput.length() > 8) {
                etInput.setError("Please Enter Valid number");
                etUserName.setError("Please Enter Valid username");
                return;
            }
            String number = Integer.toHexString(Integer.parseInt(numberInput));
            if (number.length() == 1) {
                number = "000" + number;
            } else if (number.length() == 2) {
                number = "00" + number;
            } else if (number.length() == 3) {
                number = "0" + number;
            }
            String userName = Utils.stringToHex(userNameInput);
            byte[] passwordByteArray = Utils.hexToByteArray(number);
            byte[] userNameByteArray = Utils.hexToByteArray(userName);

            byte[] startCommand = {0x02, (byte) 0x82, (byte) 0xFD, 0x0A, (byte) 0xFF};
            byte[] endCommand = {(byte) 0xFF, (byte) 0xFF, 0x0D};

            // Calculate total length for the resulting byte array
            int totalLength = passwordByteArray.length + userNameByteArray.length + startCommand.length + endCommand.length;

            // Create a new byte array to hold the combined data
            byte[] combinedByteArray = new byte[totalLength];

            // Copy data into the combinedByteArray
            int index = 0;
            System.arraycopy(startCommand, 0, combinedByteArray, index, startCommand.length);
            index += startCommand.length;
            System.arraycopy(passwordByteArray, 0, combinedByteArray, index, passwordByteArray.length);
            index += passwordByteArray.length;
            System.arraycopy(userNameByteArray, 0, combinedByteArray, index, userNameByteArray.length);
            index += userNameByteArray.length;
            System.arraycopy(endCommand, 0, combinedByteArray, index, endCommand.length);
            senMsgToBleDevice(combinedByteArray);
            dialog.dismiss();
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

    private void showToast(String message, int duration) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            bluetoothLeAdapter = ((BleAdapterService.LocalBinder) service).getService();
//            bluetoothLeAdapter.setActivityHandler(messageHandler);

            BleAdapterService.LocalBinder binder = (BleAdapterService.LocalBinder) service;
            bluetoothLeAdapter = binder.getService();
            bluetoothLeAdapter.setActivityHandler(messageHandler);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeAdapter = null;
            mBound = false;
        }
    };

    private void startServices() {
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        this.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        showToast("Ready to find BLE devices", Toast.LENGTH_SHORT);
    }

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
                    checkResponse(passwordReply, (byte[]) bundle.get("VALUE"));
                    Log.d("TAG", "Service=" + bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID).toUpperCase() +
                            " Characteristic=" + bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toUpperCase());

                    break;

            }
        }
    };


    private void checkResponse(String[] response, byte[] values) {
        if (response.length == 18) {
            String replyCode = response[4];
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
                case ReplyCode.USER_DELETION_SUCCESSFUL:
                    showToast("USER_DELETION_SUCCESSFUL", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_DELETION_FAILED:
                    showToast("USER_DELETION_FAILED", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_NOT_EXIST:
                    showToast("USER_NOT_EXIST", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_DETAILS_WITH_USERID:
                    showToast("USER_DETAILS_WITH_USERID", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.INVALID_USERID:
                    showToast("INVALID_USERID", Toast.LENGTH_SHORT);
                    break;
                 case ReplyCode.USER_DETAILS_SHARING_IN_PROGRESS:
                     Log.d("TAG", "checkResponse: "+ values);
                     byte[] id = {values[5], values[6]};
                     byte[] name = {values[7], values[8], values[9], values[10], values[11], values[12], values[13], values[14]};
                     userDetailsList.add(new UserData(name, id));

                     showToast("USER_DETAILS_SHARING_IN_PROGRESS", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.USER_DETAILS_SHARING_COMPLETE:
                    showAllUser();
                    showToast("USER_DETAILS_SHARING_COMPLETE", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.DELETE_ALL_USER_SUCCESSFUL:
                    showToast("DELETE_ALL_USER_SUCCESSFUL", Toast.LENGTH_SHORT);
                    break;
                case ReplyCode.DELETE_ALL_USER_DECLINE:
                    showToast("DELETE_ALL_USER_DECLINE", Toast.LENGTH_SHORT);
                    break;
                default:
                    showToast("Some Error", Toast.LENGTH_SHORT);
                    break;
            }
        }
    }

    private void showAllUser() {
        UserListAdapter adapter =  new UserListAdapter(this, userDetailsList);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.all_user_master_key, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        ListView lvUsers = view.findViewById(R.id.lvUsers);
        lvUsers.setAdapter(adapter);
        dialog.show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasRequiredPermissions()) {
                checkService();
                startServices();

            } else {
                requestPermissions();
            }
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
            this.startService(mServiceIntent);
            showToast(getString(R.string.service_start_successfully), Toast.LENGTH_SHORT);
        } else {
            showToast(getString(R.string.service_already_running), Toast.LENGTH_SHORT);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Arrays.stream(grantResults).allMatch(result -> result == PackageManager.PERMISSION_GRANTED)) {
                    showToast("Permissions granted.", Toast.LENGTH_SHORT);
                    checkPermissions();
                } else {
                    openPopupForSettingScreen();
                }
            }
        }
    }

    private void openPopupForSettingScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
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
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothLeAdapter.isConnected() && (bluetoothLeAdapter != null)) {
            stopServices();
        }
    }

    private void stopServices() {
        // Unbind from BleAdapterService
//        messageHandler.removeCallbacksAndMessages(null);

        if (bluetoothLeAdapter != null) {
//            bluetoothLeAdapter.removeActivityHandler();
            unbindService(serviceConnection);
            bluetoothLeAdapter = null;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        startServices();
    }


    private void showPopup() {
        startActivity(new Intent(this, UserOperationActivity.class));
    }


}