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
import android.os.StrictMode;
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
import com.kasian.trackme.data.Coordinate;
import com.kasian.trackme.property.Properties;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GPSTrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks {
    private LocationRequest locationRequest;
    private LocationCallback changeLocationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static AtomicBoolean locationUpdateStatus = new AtomicBoolean(false);

    private final IBinder mBinder = new LocationServiceBinder();

    private final CoordinateSender coordinateSender = new CoordinateSender();

    private static final GpsCoordinatesHolder coordinateHolder = GpsCoordinatesHolder.getInstance();


    private static final String TAG = "TrackMe:GPSTrackerService";

    public GPSTrackerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // Allow sending network requests (POST)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(1, notification);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "onStart");

        Toast.makeText(getApplicationContext(), "TrackMe has been started.", Toast.LENGTH_LONG).show();

        initLocationUpdates();
        scheduleLocationUpdates();
        scheduleCoordinatesCleaner();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");
        startLivenessRequests();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    public boolean getLocationUpdatesStatus() {
        return locationUpdateStatus.get();
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

    private void scheduleLocationUpdates() {
        Log.i(TAG, "scheduleLocationUpdates");

        final Runnable updateLocationChecker = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "start location checker");
                Date currentTime = Utils.getCurrentTime();
                Date startTime = Utils.getTime(Properties.startTrackingHour, Properties.startTrackingMin);
                Date stopTime = Utils.getTime(Properties.stopTrackingHour, Properties.stopTrackingMin);

                if (currentTime.after(startTime) && currentTime.before(stopTime)) {
                    Log.i(TAG, "is location checker running? status = " + locationUpdateStatus.get());
                    if (!locationUpdateStatus.get()) {
                        startLocationUpdates();
                    }
                } else {
                    Log.i(TAG, "is location checker stopped? status = " + locationUpdateStatus.get());
                    if (locationUpdateStatus.get()) {
                        stopLocationUpdates();
                    }
                }
            }
        };
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                updateLocationChecker, 0, Properties.updateLocationCheckerMin, TimeUnit.MINUTES);
    }

    private void initLocationUpdates() {
        Log.i(TAG, "initLocationUpdates");

        changeLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                sendOrCacheCoordinates(locationResult);
            }
        };

        locationRequest = Utils.createLocationRequest();

        LocationSettingsRequest.Builder locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

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

    private void sendOrCacheCoordinates(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            Log.i(TAG, "Location changed:" + location);

            Coordinate coordinate = new Coordinate(location.getLatitude(), location.getLongitude());
            if (CoordinateServerInfo.getInstance().isReady()) {
                if (sendCoordinate(coordinate)) {
                    // Send all coordinates from cache (just in case if it's not empty)
                    sendCoordinatesFromCache();
                    return;
                }
            }
            // Save coordinate to the Cache if coordiante server is not ready yet or sending failed
            cacheCoordinate(coordinate);
        }
    }

    private boolean sendCoordinate(Coordinate coordinate) {
        Log.i(TAG, "Send new coordinate to server" + coordinate);
        try {
            int responseCode = coordinateSender.send(coordinate);
            if (responseCode == Utils.HTTP_OK) {
                return true;
            } else {
                Log.e(TAG, "Can not send coordinate, responseCode=" + responseCode);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Can not send coordinate due to error", e);
        }
        return false;
    }

    private void sendCoordinatesFromCache() {
        while (!coordinateHolder.isEmpty()) {
            synchronized (coordinateHolder) {
                // Get coordinate from the queue, but do no remove from queue
                Coordinate coordinate = coordinateHolder.peek();
                if (coordinate != null) {
                    if (sendCoordinate(coordinate)) {
                        // Remove coordinate from queue in case of successfull upload to the server
                        coordinateHolder.poll();
                    }
                }
            }
        }
    }

    private synchronized void startLocationUpdates() {
        Log.i(TAG, "Start requesting location updates.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, changeLocationCallback, Looper.getMainLooper());
            locationUpdateStatus.set(true);
        } else {
            Log.e(TAG, "Location permissions turned off");
        }
    }

    private synchronized void stopLocationUpdates() {
        Log.i(TAG, "Stop requesting location updates.");
        fusedLocationProviderClient.removeLocationUpdates(changeLocationCallback);
        locationUpdateStatus.set(false);
    }

    private void cacheCoordinate(Coordinate coordinate) {
        Log.i(TAG, "Save new coordinate to cache:" + coordinate);
        coordinateHolder.add(coordinate);
    }

    private void scheduleCoordinatesCleaner() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new CoordinateHolderCleanerThread(),
                Properties.cleanCoordinatesDelayHour, Properties.cleanCoordinatesPeriodHour, TimeUnit.HOURS);
    }

    private void startLivenessRequests() {
        if (Properties.checkLivenessPeriodMin == 0) {
            return;
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "GPSTrackerPingThread is alive");
                        cacheCoordinate(new Coordinate(0, 0));
                    }
                }, 0, Properties.checkLivenessPeriodMin, TimeUnit.MINUTES);
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
                        if (currentTime - coordinate.getTimestamp() > Properties.coordinateLiveTimeMillis) {
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
