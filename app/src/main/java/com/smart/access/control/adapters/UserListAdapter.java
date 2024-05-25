package com.smart.access.control.adapters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.smart.access.control.R;
import com.smart.access.control.activities.UserData;
import com.smart.access.control.services.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class UserListAdapter extends BaseAdapter {

    Context context;
    private ArrayList<UserData> usersList;

    public UserListAdapter(Context context,ArrayList<UserData> usersList) {
        this.context = context;
        this.usersList = usersList;

    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int i) {
     return usersList.get(i);
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
        if (usersList != null) {
            UserData user = usersList.get(i);
            byte[] receivedArray = user.getUserId();
            int number = Utils.getInt(receivedArray, 0, receivedArray.length);
            viewHolder.text.setText(Integer.toString(number));
            viewHolder.bdaddr.setText(Utils.convertHexStringValue(user.getUserName()));
        }
        return view;
    }




    private String convertHexString(byte[] hexStrings) {
        StringBuilder builder = new StringBuilder();

        for (byte hexByte : hexStrings) {
            // Convert byte to unsigned int and append it to the string
            int decimalValue = hexByte & 0xFF;
            builder.append(decimalValue).append(""); // Add each decimal value to the string with a space separator
        }

        // Remove the last space character
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }

        return builder.toString();
    }




    class ViewHolder {
        TextView text;
        TextView bdaddr;

        public ViewHolder() {
        }
    }

    public void addDevice(UserData device) {
        if (!usersList.contains(device)) {
            usersList.add(device);
            notifyDataSetChanged();
        }
//        notifyDataSetChanged();
    }

    public boolean contains(UserData device) {
        return usersList.contains(device);
    }

    public UserData getDevice(int position) {
        return usersList.get(position);
    }

    public void clear() {
        usersList.clear();
    }

}
