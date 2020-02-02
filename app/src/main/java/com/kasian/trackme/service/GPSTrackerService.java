package com.kasian.trackme.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.kasian.trackme.R;
import com.kasian.trackme.Utils;
import com.kasian.trackme.coordinate.CoordinateHolder;
import com.kasian.trackme.coordinate.CoordinateSenderImpl;
import com.kasian.trackme.coordinate.CoordinateServerInfoHolder;
import com.kasian.trackme.data.Coordinate;
import com.kasian.trackme.data.CoordinateServerProperty;
import com.kasian.trackme.property.CoordinateServerInfoManager;
import com.kasian.trackme.property.CoordinateServerInfoManagerImpl;
import com.kasian.trackme.property.Properties;

import org.json.JSONException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GPSTrackerService extends IntentService {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private CoordinateServerInfoManager coordinateServerInfoManager;

    private final IBinder mBinder = new LocationServiceBinder();
    private final CoordinateSenderImpl coordinateSender = new CoordinateSenderImpl();

    private static final CoordinateHolder coordinateHolder = CoordinateHolder.getInstance();
    private static AtomicBoolean locationUpdateStatus = new AtomicBoolean(false);
    private static LocalDateTime locationLastUpdateTime;
    private static LocalDateTime locationLastSendTime;
    private static final String TAG = "TrackMe:GPSTrackerService";

    public GPSTrackerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setNetworkPolicy();
        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(Utils.NOTIFICATION_CHANNEL_ID_INT, notification);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Toast.makeText(getApplicationContext(), "TrackMe has been started.", Toast.LENGTH_LONG).show();

        initCoordinateServerInfo();
        initLocationUpdates();
        scheduleLocationUpdates();
        scheduleCoordinatesCleaner();
    }

    private void initCoordinateServerInfo() {
        coordinateServerInfoManager = new CoordinateServerInfoManagerImpl(getApplicationContext());
        CoordinateServerProperty coordinateServerProperty = coordinateServerInfoManager.getCoordinateServerProperty();
        if (coordinateServerProperty != null) {
            Log.i(TAG, "Found coordinateServerProperty=" + coordinateServerProperty);
            CoordinateServerInfoHolder.getInstance().setProperty(coordinateServerProperty);
        }
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

    public boolean getLocationUpdateStatus() {
        return locationUpdateStatus.get();
    }

    public String getLastLocationUpdateTime() {
        return locationLastUpdateTime == null ? null : Utils.getDateFormatted(locationLastUpdateTime);
    }
    public String getLastLocationSendTime() {
        return locationLastSendTime == null ? null : Utils.getDateFormatted(locationLastSendTime);
    }

    public Integer getCoordinateCacheSize() {
        return coordinateHolder.size();
    }

    public CoordinateServerProperty getCoordinateServerInfo() {
        CoordinateServerInfoHolder serverInfoHolder = CoordinateServerInfoHolder.getInstance();
        return CoordinateServerProperty.builder()
                .address(serverInfoHolder.getServer())
                .user(serverInfoHolder.getUser())
                .password(serverInfoHolder.getPassword())
                .build();
    }

    private void setNetworkPolicy() {
        // Allow sending network requests (POST)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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
            notificationManager.createNotificationChannel(channel);
            Log.i(TAG, "createNotificationChannel:" + channel);
        }
    }

    public class LocationServiceBinder extends Binder {
        public GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    private void scheduleLocationUpdates() {
        Log.i(TAG, "scheduleLocationUpdates");

        final Runnable updateLocationChecker = new Runnable() {
            @Override
            public void run() {
                Date currentTime = Utils.getCurrentTime();
                Date startTime = Utils.getTime(Properties.startTrackingHour, Properties.startTrackingMin);
                Date stopTime = Utils.getTime(Properties.stopTrackingHour, Properties.stopTrackingMin);

                if (currentTime.after(startTime) && currentTime.before(stopTime)) {
                    if (!locationUpdateStatus.get()) {
                        startLocationUpdates();
                    }
                } else {
                    if (locationUpdateStatus.get()) {
                        stopLocationUpdates();
                    }
                }
                Log.i(TAG, "service running status - " + locationUpdateStatus.get());
            }
        };

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                updateLocationChecker, 0, Properties.updateLocationCheckerMin, TimeUnit.MINUTES);
    }

    private void initLocationUpdates() {
        Log.i(TAG, "initLocationUpdates");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.i(TAG, "Location changed:" + location);
                locationLastUpdateTime = LocalDateTime.now();
                sendOrCacheCoordinates(new Coordinate(location.getLatitude(), location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        Log.w(TAG, "isNetworkEnabled=" + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        Log.w(TAG, "isGPSEnabled=" + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private void sendOrCacheCoordinates(Coordinate coordinate) {
        if (CoordinateServerInfoHolder.getInstance().isReady()) {
            if (sendCoordinate(Collections.singletonList(coordinate))) {
                // Send all coordinates from cache (just in case if it's not empty)
                sendCoordinatesFromCache();
                return;
            }
        } else {
            Log.w(TAG, "Server for uploading coordinates is not defined yet");
        }
        // Save coordinate to the Cache if coordiante server is not ready yet or sending failed
        cacheCoordinate(coordinate);
    }

    private boolean sendCoordinate(List<Coordinate> coordinates) {
        try {
            int responseCode = coordinateSender.send(coordinates);
            if (responseCode == Utils.HTTP_OK) {
                Log.i(TAG, "New coordinates have been sent to server:" + coordinates);
                locationLastSendTime = LocalDateTime.now();
                return true;
            } else {
                Log.e(TAG, "Can not send coordinates, responseCode=" + responseCode);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Can not send coordinates due to error", e);
        }
        return false;
    }

    private void sendCoordinatesFromCache() {
        while (!coordinateHolder.isEmpty()) {
            Log.i(TAG, "Send coordinates from the Cache, size=" + coordinateHolder.size());
            synchronized (coordinateHolder) {
                // Get coordinate from the queue, but do no remove from queue
                if (sendCoordinate(coordinateHolder.getAll())) {
                    // Remove coordinate from queue in case of successfull upload to the server
                    coordinateHolder.clear();
                }
            }
        }
    }

    private synchronized void startLocationUpdates() {
        Log.i(TAG, "Start requesting location updates.");
        try {
            // To resolve exception: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
            HandlerThread handlerThread = new HandlerThread("GPSTrackerServiceHandlerThread");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Properties.locationInterval, Properties.locationDistance,
                    locationListener, looper);

            locationUpdateStatus.set(true);
            Log.i(TAG, "Location updates has been started successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Location permissions are turned off. Please turn it on and restart the application");
        } catch (Exception e) {
            Log.e(TAG, "Can not request location updates", e);
        }
    }

    private synchronized void stopLocationUpdates() {
        Log.i(TAG, "Stop requesting location updates.");
        locationManager.removeUpdates(locationListener);
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
                        sendOrCacheCoordinates(new Coordinate(0, 0));
                    }
                }, 0, Properties.checkLivenessPeriodMin, TimeUnit.MINUTES);
    }

    private class CoordinateHolderCleanerThread implements Runnable {
        @Override
        public void run() {
            Log.i(TAG, "Start coordinate cleaner");
            long currentTime = System.currentTimeMillis();
            List<Coordinate> coordiantesToRemove = new ArrayList<>();

            synchronized (coordinateHolder) {
                for (Coordinate coordinate : coordinateHolder.getAll()) {
                    if (currentTime - coordinate.getTimestamp() > Properties.coordinateLiveTimeMillis) {
                        coordiantesToRemove.add(coordinate);
                    }
                }
                if (coordiantesToRemove.isEmpty()) {
                    Log.i(TAG, "Nothing to remove for now");
                } else {
                    coordinateHolder.removeAll(coordiantesToRemove);
                    Log.w(TAG, "Remove coordinates from queue due to timeout:" + coordiantesToRemove);
                }
            }
        }
    }
}
