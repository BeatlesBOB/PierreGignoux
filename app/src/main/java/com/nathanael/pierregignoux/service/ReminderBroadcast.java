package com.nathanael.pierregignoux.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nathanael.pierregignoux.R;

import static android.content.Context.MODE_PRIVATE;

public class ReminderBroadcast extends BroadcastReceiver {

    private String title;
    private String texte;
    private String param;
    private double parameternb;



    @Override
    public void onReceive(Context context, Intent intent) {
        title = intent.getStringExtra("title");
        texte = intent.getStringExtra("texte");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"NotificationDefis")
                .setSmallIcon(R.drawable.ic_launch_final)
                .setContentTitle(title)
                .setContentText(texte)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200,builder.build());




    }
}
