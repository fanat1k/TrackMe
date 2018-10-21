package com.pereginiak.trackme;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.concurrent.Executors;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class GPSTrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "GPSTracker";

    //TODO(kasian @2018-10-21): what's this for?
    private static final int PERMISSION_REQUEST_CODE = 1000;

    //TODO(kasian @2018-09-26):
    private static final int FOREGROUND_ID = 1338;

    private static long UPDATE_INTERVAL = 60 * 1000;

    private static long FASTEST_INTERVAL = 10 * 2000;

    private FusedLocationProviderClient fusedLocationProviderClient;

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
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        //initFusedLocationProvider();

        initLocationUpdates();
    }

    @NonNull
    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
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
        LocationRequest mLocationRequest = createLocationRequest();
/*
        //TODO(kasian @2018-09-30): need?
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
*/
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i(TAG, "location changed:" + locationResult);
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.e(TAG, "GPS: permissions denied");
            Log.e(TAG, "ACCESS_FINE_LOCATION=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
            Log.e(TAG, "ACCESS_COARSE_LOCATION=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION));
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
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

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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

        //startForeground(FOREGROUND_ID, buildForegroundNotification());

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
        private static final int TIMEOUT = 15000;

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
