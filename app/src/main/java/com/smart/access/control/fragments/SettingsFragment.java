package com.smart.access.control.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.access.control.R;
import com.smart.access.control.activities.SettingActivity;
import com.smart.access.control.adapters.BleListAdapter;
import com.smart.access.control.adapters.CustomAdapter;
import com.smart.access.control.modals.SettingsMenu;
import com.smart.access.control.utils.SessionManager;

import java.util.ArrayList;
import java.util.Set;


public class SettingsFragment extends Fragment implements CustomAdapter.OnItemClickListener {


    private View view;
    private TextView tv_name;
    private TextView tvDetails;
    private String tag;
    private RecyclerView rvMenu;
    private CustomAdapter customAdapter;
    private ArrayList<SettingsMenu> settingsMenus;
    private Context context;
    private SessionManager sessionManager;
    private ListView listView;
    private BleListAdapter bleDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private Toast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        context =getContext();
        sessionManager =new SessionManager(context);
        getData();

        setRecyclerView();

//        setPairedDevice();
        return view;
    }

//    private void setPairedDevice() {
//        bleDeviceListAdapter = new BleListAdapter(context);
//        listView = view.findViewById(R.id.deviceList);
//        listView.setAdapter(bleDeviceListAdapter);
//
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
//            }
//        }
//    }

//    private void showPairedDevices() {
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                bleDeviceListAdapter.addDevice(device);
//            }
//        } else {
////            bleDeviceListAdapter.add("No paired devices found");
//        }
//    }


    private void setRecyclerView() {
        rvMenu = view.findViewById(R.id.rvMenu);
        tvDetails = view.findViewById(R.id.tvDetails);
        tv_name = view.findViewById(R.id.tv_name);

        tv_name.setText(sessionManager.getLoginData(SessionManager.KEY_NAME));
        tvDetails.setText(sessionManager.getLoginData(SessionManager.KEY_EMAIL));

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        rvMenu.setLayoutManager(mLayoutManager);
        customAdapter = new CustomAdapter(settingsMenus);
        rvMenu.setAdapter(customAdapter);
        customAdapter.setOnItemClickListener(this);


    }

    private void getData() {
        settingsMenus = new ArrayList<>();
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_question_mark_24, "FAQs", "Frequently asked questions can be found here"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_info_24, "About Us", "Know more about us"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_note_24, "Terms & Conditions", "Terms of Service, Usage & other conditions"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_remove_red_eye_24, "Privacy Policy", "Data policy, consents & more"));
        settingsMenus.add(new SettingsMenu(R.drawable.ic_baseline_logout_24, "LOGOUT", "Logout from this device"));
    }

    @Override
    public void onItemClick(SettingsMenu item) {
        String tag;

        if(item.getTitle().equals("LOGOUT")){
            sessionManager.logoutUser();
        }
        else {
            Intent in = new Intent(context, SettingActivity.class);
            in.putExtra("tag", item.getTitle());
            context.startActivity(in);
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

}