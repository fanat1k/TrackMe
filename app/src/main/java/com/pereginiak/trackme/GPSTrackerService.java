package com.pereginiak.trackme;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class GPSTrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks {

    //TODO(kasian @2018-10-21): what's this for?
    private static final int PERMISSION_REQUEST_CODE = 1000;

    //TODO(kasian @2018-09-26): need when start Service with foreground notification,
    // (trying to encrease quantity of gps requests when app is on backgound)
    private static final int FOREGROUND_ID = 1338;

    private FusedLocationProviderClient fusedLocationProviderClient;

    LocationRequest locationRequest;

    //TODO(romanpe @2018-10-23):
    private LocationCallback locationCallback;

    //private static final LocalTime START_TIME = LocalTime.of(8, 0);

    //private static final LocalTime STOP_TIME = LocalTime.of(21, 0);

    private static final LocalTime START_TIME = LocalTime.of(16, 22);
    private static final LocalTime STOP_TIME = LocalTime.of(16, 23);

    private static final long LCOATION_UPDATE_PERIOD = 3;

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

        //TODO(romanpe @2018-10-24):
        scheduleLocationUpdates();
    }


    private void scheduleLocationUpdates() {
        //scheduleJob(LocationUpdatesStartJob.class, START_TIME);
        //scheduleJob(LocationUpdatesStopJob.class, STOP_TIME);

        //LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), time);

        LocalDateTime currentDateTime = LocalDateTime.now();

        Log.i(TAG,"scheduleLocationUpdates, currentDateTime=" + currentDateTime);

        //TODO(romanpe @2018-10-24): set to 24hours
        TimeUnit timeUnit = TimeUnit.MINUTES;

        long starLocationUpdatesDelay = getExecutionDelay(currentDateTime, START_TIME);
        Log.i(TAG, "starLocationUpdatesDelay(min)=" + starLocationUpdatesDelay);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> startLocationUpdates(),
                starLocationUpdatesDelay,
                LCOATION_UPDATE_PERIOD,
                timeUnit
        );

        long stopLocationUpdatesDelay = getExecutionDelay(currentDateTime, STOP_TIME);
        Log.i(TAG, "stopLocationUpdatesDelay(min)=" + stopLocationUpdatesDelay);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> stopLocationUpdates(),
                stopLocationUpdatesDelay,
                LCOATION_UPDATE_PERIOD,
                timeUnit
        );

        if (currentDateTime.toLocalTime().isAfter(START_TIME) && currentDateTime.toLocalTime().isBefore(STOP_TIME)) {
            startLocationUpdates();
        }
    }

    //Return delay in MINUTES
    private long getExecutionDelay(LocalDateTime currentDateTime, LocalTime startTime) {
        LocalDateTime nextRunTime = LocalDateTime.of(currentDateTime.toLocalDate(), startTime);
        if (currentDateTime.isAfter(nextRunTime)) {
            nextRunTime = nextRunTime.plusDays(1);
        }

        return Duration.between(currentDateTime, nextRunTime).toMinutes();
    }

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


/*
    private void startListening() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);

            if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
        isRunning = true;
    }
*/

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.e(TAG, "GPS: permissions denied");
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    //TODO(romanpe @2018-10-24): synchronized?
    private synchronized void stopLocationUpdates() {
        Log.i(TAG, "Stop request location updates");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


/*    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

        //TODO(romanpe @2018-10-23): is it make sense? how to start it properly then
        //startForeground(FOREGROUND_ID, buildForegroundNotification());
        //TODO(romanpe @2018-10-24): why does it start in this method?
        Executors.newSingleThreadExecutor().submit(new GPSTrackerThread());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "!!!onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "!!!onConnectionSuspended");
    }

    private class GPSTrackerThread implements Runnable {
        //TODO(romanpe @2018-10-23): set to 1 min or more
        private static final int TIMEOUT = 15000;

        int count = 0;

        @Override
        public void run() {
            while (true) {
                //Log.i(TAG, "GPSTrackerThread ping...");
                sendBroadcast(0, 0);

                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    Log.i(TAG, "ERROR:" + e.getMessage());
                }

                //if (count++ > 5) {
                //    stopLocationUpdates();
                //}
            }
        }
    }

    private void sendBroadcast(double latitude, double longitude) {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

        Intent intent = new Intent("location");
        intent.setAction("com.pereginiak.LOCACTION_CHANGED");
        intent.putExtra("time", currentDateTime);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);

        Log.d(TAG, "send new location broadcast:" + currentDateTime + " - " + latitude + " : " + longitude);
        sendBroadcast(intent);
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
