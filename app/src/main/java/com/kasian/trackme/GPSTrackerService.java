package com.kasian.trackme;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import javax.security.auth.login.LoginException;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class GPSTrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks {

    //TODO(kasian @2018-09-26): need when start Service with foreground notification,
    // (trying to encrease quantity of gps requests when app is on backgound)
    private static final int FOREGROUND_ID = 1338;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationRequest locationRequest;

    //TODO(romanpe @2018-10-23):
    private LocationCallback locationCallback;

    //TODO(kasian @2018-11-11): change to 8 and 21 accordingly
    private static final int START_TIME = 8;

    private static final int STOP_TIME = 21;

    // TODO: 11.11.2018 set to 24 hours
    //private static final int PERIOD = 60*60*24*1000;
    private static final int PERIOD = 60*60*1000;

    private static final long LOCATION_REQUEST_UPDATE_INTERVAL = 60 * 1000;

    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 10 * 2000;

    private static final String TAG = "GPSTracker";

    public GPSTrackerService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        Toast.makeText(getApplicationContext(), "Foreground service has been started.", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "onStart");

        initLocationUpdates();
        scheduleLocationUpdates();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "!!!onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "!!!onConnectionSuspended");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void scheduleLocationUpdates() {

        class StartLocationUpdates extends TimerTask {
            @Override
            public void run() {
                startLocationUpdates();
            }
        }

        class StopLocationUpdates extends TimerTask {
            @Override
            public void run() {
                stopLocationUpdates();
            }
        }

        // TODO: 11.11.2018
//        Date startTime = getRunTime(START_TIME);
//        Date stopTime = getRunTime(STOP_TIME);
        Date startTime = getRunTime(23, 48);
        Date stopTime = getRunTime(00, 10);

        Date currentTime = Calendar.getInstance().getTime();
        if (startTime.before(currentTime)) {
            startTime = addOneDay(startTime);
        }
        if (stopTime.before(currentTime)) {
            stopTime = addOneDay(stopTime);
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new StartLocationUpdates(), startTime, PERIOD);
        timer.scheduleAtFixedRate(new StopLocationUpdates(), stopTime, PERIOD);
    }

    private Date addOneDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    // TODO: 11.11.2018 delete
    private Date getRunTime(int runHour, int runMin) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, runHour);
        calendar.set(Calendar.MINUTE, runMin);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }

/*
    private Date getRunTime(int runHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, runHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }
*/

    @NonNull
    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        //TODO: check if it's optimal
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        return locationRequest;
    }

    private void initLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.i(TAG, "location changed:" + location);
                sendBroadcast(location.getLatitude(), location.getLongitude());
            }
        };

        locationRequest = createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    //TODO(romanpe @2018-10-24): synchronized?
    private synchronized void startLocationUpdates() {
        Log.i(TAG, "Start request location updates");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Log.e(TAG, "Location permissions turned off");
        }
    }

    //TODO(romanpe @2018-10-24): synchronized?
    private synchronized void stopLocationUpdates() {
        Log.i(TAG, "Stop request location updates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

        //TODO(romanpe @2018-10-23): is it make sense? how to start it properly then
        //startForeground(FOREGROUND_ID, buildForegroundNotification());

        //TODO(romanpe @2018-10-24): why does it start in this method?
        Executors.newSingleThreadExecutor().submit(new GPSTrackerPingThread());
    }

    private class GPSTrackerPingThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                Log.i(TAG, "GPSTrackerPingThread ping...");
                sendBroadcast(0, 0);
                try {
                    Thread.sleep(60*1000);
                } catch (InterruptedException e) {
                    Log.i(TAG, "ERROR:" + e.getMessage());
                }
            }
        }
    }

    private void sendBroadcast(double latitude, double longitude) {
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Intent intent = new Intent("location");
        intent.setAction("com.kasian.LOCACTION_CHANGED");
        intent.putExtra("time", currentDateTime);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);

        Log.d(TAG, "send new location broadcast:" + currentDateTime + " - " + latitude + " : " + longitude);
        sendBroadcast(intent);
    }

    // TODO: 11.11.2018 ???
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

/*
    private void initProperties(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
