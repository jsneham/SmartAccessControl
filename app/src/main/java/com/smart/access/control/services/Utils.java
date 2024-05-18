package com.smart.access.control.services;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    public static boolean isMyServiceRunning(Class serviceClass, Activity mActivity) {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName() == service.service.getClassName()) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    public static boolean isLocationEnabledOrNot(Context context) {
        LocationManager locationManager = null;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    public static void showAlertLocation(Context context, String title, String message, String btnText) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, btnText, (DialogInterface.OnClickListener) (dialogInterface, i) -> {
            dialogInterface.dismiss();
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        });

        alertDialog.show();
    }



    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR");
                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI");
                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET");
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean checkAndRequestPermissions(final Activity context) {
        int WExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String result = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return result;
    }

    public static String convertDate(String dateString) {
        if (dateString.equals("") || dateString.isEmpty()) return "";
        String strDate = "";
        DateFormat sdf = new SimpleDateFormat("dd MMM hh:mm a");
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateString);
            strDate = sdf.format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    public static String convertDateTo(String dateString) {
        String datePattern = "\\d{4}-\\d{1,2}-\\d{1,2}";
        if (dateString.equals("") || dateString.isEmpty()) return "";
        String strDate = "";
        DateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
//            if(dateString.matches(datePattern))
            Date date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateString);
            strDate = sdf.format(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }


    public static boolean checkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String capitalize(String str)
    {
        String words[]=str.split("\\s");
        String capitalizeWord="";
        for(String w:words){
            String first=w.substring(0,1);
            String afterfirst=w.substring(1);
            capitalizeWord+=first.toUpperCase()+afterfirst+" ";
        }
        return capitalizeWord.trim();
    }


    public static String stringToHex(String input) {
        // Convert string to byte array
        byte[] bytes = input.getBytes();

        // Convert byte array to hexadecimal string
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexStringBuilder.append(String.format("%02X", b));
        }

        return hexStringBuilder.toString();
    }

    public static byte[] hexToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }


    public static String convertByteHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }

    public static String[] convertByteHexArray(byte[] bytes) {
        String[] hexStrings = new String[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            hexStrings[i] = String.format("%02X", bytes[i]);
        }
        return hexStrings;
    }

    public static byte[] convertHexByte(String hexString) {
        // Remove any non-hex characters from the string
        hexString = hexString.replaceAll("\\s+", ""); // Remove whitespace
        hexString = hexString.replaceAll("[^A-Fa-f0-9]", "");

        // If the string length is not even, add a leading zero
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        // Convert each pair of hex characters to a byte
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            String byteString = hexString.substring(i, i + 2);
            byteArray[i / 2] = (byte) Integer.parseInt(byteString, 16);
        }

        return byteArray;
    }


    public static String byteToHex(byte b) {
        return String.format("%02X", b);
    }

}
