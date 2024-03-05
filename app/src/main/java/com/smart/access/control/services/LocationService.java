package com.smart.access.control.services;

import android.Manifest;
import android.app.Service;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {
    int counter = 0;
    double latitude = 0.0;
    double longitude = 0.0;
//    SessionManager sm;
    private String TAG = "LocationService";


    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel();
        else startForeground(1, new Notification());
//        sm = new SessionManager(this);
        requestLocationUpdates();
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChanel() {
        String NOTIFICATION_CHANNEL_ID = "com.getlocationbackground";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        );
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running count::" + counter)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                int count = counter++;
                if (!(latitude == 0.0 && longitude == 0.0)) {
                    Log.d(
                            "Location::",
                            latitude + ":::" + longitude + " Count" +
                                    count
                    );

//                    addLocation(latitude, longitude);
                }
            }
        };
        timer.schedule(
                timerTask,
                0,
                30000
        ); //1 * 60 * 1000 1 minute
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();
    }

    private void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private Timer timer = null;
    private TimerTask timerTask = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_NO_POWER);
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d("Location Service", "location update $location");

//                        sm.setUserData(SessionManager.Latitude, String.valueOf(latitude));
//                        sm.setUserData(SessionManager.Longitude, String.valueOf(longitude));
                    }
                }
            }, null);;
        }

    }



//    private void addLocation(double latitude_point, double longitude_point) {
//        SessionManager sm =  new SessionManager(this);
//        sm.setUserData(SessionManager.Latitude, "19.055155");
//        sm.setUserData(SessionManager.Longitude, "72.9014567");
//
//        try {
//            APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
//            Call<RegisterResponse> call = apiInterface.getRegisterLocation(sm.getUserData(SessionManager.user_logged_id),String.valueOf(latitude_point), String.valueOf(longitude_point));
//            call.enqueue(new Callback<RegisterResponse>() {
//                @Override
//                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
//                    if (response.isSuccessful()) {
//                        getAddress(latitude_point,longitude_point,sm);
//                    } else {
//
//                    }
//
//
//                }
//
//                @Override
//                public void onFailure(Call<RegisterResponse> call, Throwable t) {
//                    call.cancel();
//
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//
//
//
//    }
//
//    private void getAddress(double latitude_point, double longitude_point, SessionManager sm) {
//        Geocoder geocoder;
//        List<Address> addresses;
//        geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            addresses = geocoder.getFromLocation(latitude,longitude,1);
//            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state= addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName();
//
//            Log.d(TAG, "getAddress: $address" );
//            sm.setUserData(SessionManager.CADDRESS, address);
//            sm.setUserData(SessionManager.Latitude, String.valueOf(latitude));
//            sm.setUserData(SessionManager.Longitude, String.valueOf(longitude));
//
//            showText(address);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void showText(String address) {
        Toast.makeText(this, "Your Current Location : " +address,Toast.LENGTH_LONG).show();
    }



}