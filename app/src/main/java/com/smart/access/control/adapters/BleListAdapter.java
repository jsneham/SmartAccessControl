package com.smart.access.control.adapters;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.smart.access.control.R;

import java.util.ArrayList;

public class BleListAdapter extends BaseAdapter {

    Context context;
    private ArrayList<BluetoothDevice> ble_devices;

    public BleListAdapter(Context context) {
        this.context = context;
        ble_devices = new ArrayList<BluetoothDevice>();

    }

    @Override
    public int getCount() {
        return ble_devices.size();
    }

    @Override
    public Object getItem(int i) {
     return ble_devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View views, ViewGroup viewGroup) {
        View view = views;
        ViewHolder viewHolder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.text = view.findViewById(R.id.textView);
            viewHolder.bdaddr = view.findViewById(R.id.bdaddr);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (ble_devices != null) {
            BluetoothDevice device = ble_devices.get(i);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.text.setText(deviceName);
            } else {
                viewHolder.text.setText("unknown device");
            }
//            viewHolder.bdaddr.setText(device.getAddress());
        }
        return view;
    }



    class ViewHolder {
        TextView text;
        TextView bdaddr;

        public ViewHolder() {
        }
    }

    public void addDevice(BluetoothDevice device) {
        if (!ble_devices.contains(device)) {
            ble_devices.add(device);
            notifyDataSetChanged();
        }
//        notifyDataSetChanged();
    }

    public boolean contains(BluetoothDevice device) {
        return ble_devices.contains(device);
    }

    public BluetoothDevice getDevice(int position) {
        return ble_devices.get(position);
    }

    public void clear() {
        ble_devices.clear();
    }

}
