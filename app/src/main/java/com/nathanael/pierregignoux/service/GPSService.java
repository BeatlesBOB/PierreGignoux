package com.nathanael.pierregignoux.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nathanael.pierregignoux.MapsActivity;
import com.nathanael.pierregignoux.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GPSService extends Service {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Service GPS";

    public static FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    public static LocationCallback locationCallback;
    private List<Location> locationList = new ArrayList<>();
    private float distancetracking;
    private int j = 0;
    BigDecimal finaldist = new BigDecimal("0");

    Timer chrono = new Timer();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        startForeground();

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        locationList.clear();
        chrono.cancel();
    }

    private void startForeground() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        chrono.schedule(new TimerTask() {
            int seconde = 0;
            int minute = 0;
            @Override
            public void run() {
                seconde++;
                if(seconde==60)
                {
                    seconde=0;
                    minute++;
                }
                if(minute==60)
                {
                    minute=0;
                }
                Intent i2 = new Intent("chrono_update");
                String temps = +minute+" min "+seconde+" sec";
                i2.putExtra("chrono", temps);
                sendBroadcast(i2);
            }
        }, 0, 1000);


        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                finaldist = BigDecimal.valueOf(0);
                locationList.add(locationResult.getLastLocation());
                while (j < locationList.size() - 1) {
                    Location loc1 = locationList.get(j);
                    Location loc2 = locationList.get(j + 1);
                    distancetracking += loc1.distanceTo(loc2);
                    j++;
                }
                finaldist = BigDecimal.valueOf(distancetracking/1000);
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", finaldist);
                sendBroadcast(i);
                notification(finaldist+ " Km");

            }
        };
        oncreatelocation();

    }

    private void notification(String distance) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent notificationIntent = new Intent(this, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIF_CHANNEL_ID,"PierreGignoux",NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
            startForeground(NOTIF_ID, new Notification.Builder(this, NOTIF_CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launch_final)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(distance)
                    .setContentIntent(pendingIntent)
                    .build());

        }else {

            Intent notificationIntent = new Intent(this, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                    NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(distance)
                    .setContentIntent(pendingIntent)
                    .build());
        }

    }

    private void oncreatelocation() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

}