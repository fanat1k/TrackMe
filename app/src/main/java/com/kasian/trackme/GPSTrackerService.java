package com.kasian.trackme;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GPSTrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks {
    private LocationRequest locationRequest;
    private LocationCallback changeLocationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GpsCoordinatesHolder coordinateHolder = GpsCoordinatesHolder.getInstance();

    private final IBinder mBinder = new LocationServiceBinder();

    private static final String TAG = "GPSTracker:GPSTrackerService";

    public GPSTrackerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(1, notification);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "onStart");

        Toast.makeText(getApplicationContext(), "GPSTrackerService service has been started.", Toast.LENGTH_LONG).show();

        initLocationUpdates();
        scheduleLocationUpdates();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new CoordinateHolderCleanerThread(), 24, 1, TimeUnit.HOURS);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

        // TODO: 12.11.2018 tmp for testing
        // TODO: 08.03.2019 move this ping to onCreate ? Make sure that onHandleIntent is run only once!
        Runnable pingRequest = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.i(TAG, "GPSTrackerPingThread ping...");
                    saveCoordinates(0, 0);
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "ERROR:" + e.getMessage());
                    }
                }
            }
        };
        Executors.newSingleThreadExecutor().submit(pingRequest);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved");
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "onLowMemory");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getAllCoordinates() {
        if (coordinateHolder.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        while (!coordinateHolder.isEmpty()) {
            Coordinate coordinate = coordinateHolder.poll();
            if (coordinate != null) {
                stringBuilder
                        .append(coordinate.getDate())
                        .append(Utils.DELIMITER)
                        .append(coordinate.getTime())
                        .append(Utils.DELIMITER)
                        .append(coordinate.getLatitude())
                        .append(Utils.DELIMITER)
                        .append(coordinate.getLongitude())
                        .append("\n")
                ;
            }
        }

        return stringBuilder.toString();
    }

    private Notification createNotification() {
        Notification.Builder builder = new Notification.Builder(this, Utils.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true);
        return builder.build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                Utils.NOTIFICATION_CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            Log.e(TAG, "Can not create Notification Channel: notificationManager is null");
        } else {
            Log.i(TAG, "createNotificationChannel:" + channel);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class LocationServiceBinder extends Binder {
        GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void scheduleLocationUpdates() {
        Log.i(TAG, "scheduleLocationUpdates");

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

        Date startTime = Utils.getRunTime(Utils.START_TIME);
        Date stopTime = Utils.getRunTime(Utils.STOP_TIME);
        Date currentTime = Calendar.getInstance().getTime();

        if (currentTime.after(stopTime)) {
            stopTime = Utils.addOneDay(stopTime);
        }

        if (currentTime.after(startTime)) {
            startTime = Utils.addOneDay(startTime);

            if (currentTime.before(stopTime)) {
                // TODO: 02.03.2019 is it ok to run it in main thread?
                startLocationUpdates();
            }
        }

        Timer timer = new Timer();
        Log.i(TAG, "Schedule tracking location start at: " + startTime);
        timer.scheduleAtFixedRate(new StartLocationUpdates(), startTime, Utils.PERIOD);

        Log.i(TAG, "Schedule tracking location stop at: " + stopTime);
        timer.scheduleAtFixedRate(new StopLocationUpdates(), stopTime, Utils.PERIOD);
    }

    private void initLocationUpdates() {
        Log.i(TAG, "initLocationUpdates");

        changeLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // TODO: 05.03.2019 debug only:
                Location lastLocation = locationResult.getLastLocation();
                Log.i(TAG, "Last location:" + lastLocation);
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, "Location changed:" + location);
                    saveCoordinates(location.getLatitude(), location.getLongitude());
                }
            }
        };

        locationRequest = Utils.createLocationRequest();

        LocationSettingsRequest.Builder locationSettingsBuilder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsBuilder.build());

        // TODO: 05.03.2019 for debug only
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(TAG, "locationSettingsResponse:onSuccess=" + locationSettingsResponse);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG,"locationSettingsResponse:onFailure:e=" + e);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    //TODO(romanpe @2018-10-24): synchronized?
    private synchronized void startLocationUpdates() {
        Log.i(TAG, "Start requesting location updates.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, changeLocationCallback, Looper.getMainLooper());
        } else {
            Log.e(TAG, "Location permissions turned off");
        }
    }

    //TODO(romanpe @2018-10-24): synchronized?
    private synchronized void stopLocationUpdates() {
        Log.i(TAG, "Stop requesting location updates.");
        fusedLocationProviderClient.removeLocationUpdates(changeLocationCallback);
    }

    private void saveCoordinates(double latitude, double longitude) {
        Date time = Calendar.getInstance().getTime();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(time);
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(time);

        Coordinate coordinates = new Coordinate(System.currentTimeMillis(), latitude, longitude, currentDate, currentTime);
        Log.d(TAG, "Save new coordinates:" + coordinates);

        coordinateHolder.add(coordinates);
    }

    private class CoordinateHolderCleanerThread implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "Start coordinate cleaner");
            long currentTime = System.currentTimeMillis();
            while (!coordinateHolder.isEmpty()) {
                synchronized (coordinateHolder) {
                    Coordinate coordinate = coordinateHolder.peek();
                    if (coordinate != null) {
                        if (currentTime - coordinate.getTimestamp() > Utils.COORDINATE_LIVE_TIME) {
                            Log.i(TAG, "Remove coordinate from queue due to timeout:" + coordinate);
                            coordinateHolder.poll();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
}
