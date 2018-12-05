package com.kasian.trackme;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class GPSTrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks {

    private LocationRequest locationRequest;

    private LocationCallback changeLocationCallback;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private GpsCoordinatesHolder coordinateHolder = GpsCoordinatesHolder.getInstance();

    private static final long COORDINATE_LIVE_TIME = 60*60*24*1000;             // 24 hours

    private static final long LOCATION_REQUEST_UPDATE_INTERVAL = 30 * 1000;     // 30 secs

    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 10 * 1000;    // 10 secs

    private static final int START_TIME = 6;            // 8AM

    private static final int STOP_TIME = 4;            // 9PM

    private static final int PERIOD = 60*60*24*1000;    // 24 hours

    private static final String DELIMITER = ";";

    //TODO(kasian @2018-09-26): need when start Service with foreground notification
    // (trying to encrease quantity of gps requests when app is on backgound)
    private static final int FOREGROUND_ID = 1338;

    private static final String TAG = "GPSTracker";

    private final IBinder mBinder = new LocalBinder();

    public GPSTrackerService() {
        super(TAG);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "onStart");

        Toast.makeText(getApplicationContext(), "Foreground service has been started.", Toast.LENGTH_LONG).show();

        initLocationUpdates();
        scheduleLocationUpdates();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new CoordinateHolderCleanerThread(), 24, 1, TimeUnit.HOURS);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

        //TODO(romanpe @2018-10-23): is it make sense? how to start it properly then
        //startForeground(FOREGROUND_ID, buildForegroundNotification());

        // TODO: 12.11.2018 tmp for testing
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
        Log.i(TAG, "!!!onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "!!!onConnectionSuspended");
    }

    public class LocalBinder extends Binder {
        GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getAllCoordinates() {
        if (coordinateHolder.isEmpty()) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        while (!coordinateHolder.isEmpty()) {
            Coordinate coordinate = coordinateHolder.poll();
            if (coordinate != null) {
                stringBuilder
                        .append(coordinate.getDate())
                        .append(DELIMITER)
                        .append(coordinate.getTime())
                        .append(DELIMITER)
                        .append(coordinate.getLatitude())
                        .append(DELIMITER)
                        .append(coordinate.getLongitude())
                        .append("\n")
                ;
            }
        }

        return stringBuilder.toString();
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

        Date startTime = getRunTime(START_TIME);
        Date stopTime = getRunTime(STOP_TIME);
        Date currentTime = Calendar.getInstance().getTime();

        if (currentTime.after(stopTime)) {
            stopTime = addOneDay(stopTime);
        }

        if (currentTime.after(startTime)) {
            startTime = addOneDay(startTime);

            if (currentTime.before(stopTime)) {
                startLocationUpdates();
            }
        }

        Timer timer = new Timer();
        Log.i(TAG, "Schedule tracking location start at: " + startTime);
        timer.scheduleAtFixedRate(new StartLocationUpdates(), startTime, PERIOD);
        Log.i(TAG, "Schedule tracking location stop at: " + stopTime);
        timer.scheduleAtFixedRate(new StopLocationUpdates(), stopTime, PERIOD);
    }

    private Date addOneDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    private Date getRunTime(int runHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, runHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
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

    private void initLocationUpdates() {
        changeLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.i(TAG, "location changed:" + location);
                saveCoordinates(location.getLatitude(), location.getLongitude());
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

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, changeLocationCallback, Looper.getMainLooper());
        } else {
            Log.e(TAG, "Location permissions turned off");
        }
    }

    //TODO(romanpe @2018-10-24): synchronized?
    private synchronized void stopLocationUpdates() {
        Log.i(TAG, "Stop request location updates");
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

    // TODO: 11.11.2018 need?
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

    private class CoordinateHolderCleanerThread implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "Start coordinate cleaner");
            long currentTime = System.currentTimeMillis();
            while (!coordinateHolder.isEmpty()) {
                synchronized (coordinateHolder) {
                    Coordinate coordinate = coordinateHolder.peek();
                    if (coordinate != null) {
                        if (currentTime - coordinate.getTimestamp() > COORDINATE_LIVE_TIME) {
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
