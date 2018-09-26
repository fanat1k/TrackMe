package com.pereginiak.trackme;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;

public class GPSTracker extends IntentService {

    private static final String TAG = "GPSTracker";

    //TODO(kasian @2018-09-26):
    private static final int FOREGROUND_ID = 1338;

    public GPSTracker() {
        super(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Foreground service has been started.", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

        startForeground(FOREGROUND_ID, buildForegroundNotification());

        Executors.newSingleThreadExecutor().submit(new GPSTrackerThread());
    }

    private class GPSTrackerThread implements Runnable {
        private static final int TIMEOUT = 2000;

        @Override
        public void run() {
            while (true) {
                Log.i(TAG, "GPSTrackerThread ping...");
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    Log.i(TAG, "ERROR:" + e.getMessage());
                }
            }
        }
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channelId");

        notificationBuilder
                .setOngoing(true)
                .setContentTitle(getString(R.string.downloading))
                .setContentText("Content Text")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker(getString(R.string.downloading));

        return notificationBuilder.build();
    }
}
