package com.nathanael.pierregignoux.service;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.location.Criteria;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.nathanael.pierregignoux.MapsActivity;
import com.nathanael.pierregignoux.R;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class GPSService extends Service {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Service GPS";

    private float distancetracking;
    private int j = 0;
    BigDecimal finaldist = new BigDecimal("0");
    private LocationListener listener;
    private LocationManager locationManager;
    private String provider;
    private Location lastLocation, newLocation;

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
        locationManager.removeUpdates(listener);
        chrono.cancel();

    }

    @SuppressLint("MissingPermission")
    private void startForeground() {

        chrono.schedule(new TimerTask() {
            int seconde = 0;
            int minute = 0;
            int heure = 0;

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
                    heure++;
                }
                Intent i2 = new Intent("chrono_update");
                String temps = heure+" h "+minute+" min "+seconde+" sec";
                i2.putExtra("chrono", temps);
                sendBroadcast(i2);
            }
        }, 0, 1000);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location locationResult) {

                //Verify accuracy of this location in meters.
                if (locationResult.getAccuracy() > 50){
                    Log.d("alloaccu","JPPPPP");
                    return;
                }

                lastLocation = newLocation;
                newLocation = locationResult;


                if (lastLocation == null)   //first gps triger.
                    distancetracking = 0;
                else
                    distancetracking += lastLocation.distanceTo(newLocation);

                finaldist = BigDecimal.valueOf(distancetracking/1000);
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", finaldist);
                sendBroadcast(i);
                notification(finaldist+ " Km");

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider,1000,0,listener);
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
                    .setSmallIcon(R.drawable.ic_launch_final)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(distance)
                    .setContentIntent(pendingIntent)
                    .build());
        }

    }


}