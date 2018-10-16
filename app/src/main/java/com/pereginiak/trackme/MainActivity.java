package com.pereginiak.trackme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.location.*;

import java.io.IOException;
import java.io.InputStream;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GPSTracker";
    private static final int PERMISSION_REQUEST_CODE = 1000;

    private static long UPDATE_INTERVAL = 10 * 1000;
    private static long FASTEST_INTERVAL = 2 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream resourceAsStream = getClass().getResourceAsStream("111.txt");

        try {
            InputStream is = getAssets().open("abc/222.txt");
            int read = is.read();
        } catch (IOException e) {
            e.printStackTrace();
        }


        checkPermissions();
        initFusedLocationProvider();

        //TODO(kasian @2018-09-30): is this a right place?
        //startService();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //finish();
    }

    private void startService() {

        Intent service = new Intent(this, GPSTracker.class);
        //ContextCompat.startForegroudService(this, service);
        startService(service);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permissions are OK");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.w(TAG, "Permissions was requested");
            Log.i(TAG, "ACCESS_FINE_LOCATION=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
            Log.i(TAG, "ACCESS_COARSE_LOCATION=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION));
        }
    }


    private void initFusedLocationProvider() {
        Log.i(TAG, "initFusedLocation");
        LocationRequest locationRequest = createLocationRequest();

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

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.i(TAG, "Location changed:" + locationResult);
                }
            };

            Log.i(TAG, "requestLocationUpdates");
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } else {
            Log.e(TAG, "GPS: permissions denied");
            Log.e(TAG, "ACCESS_FINE_LOCATION=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
            Log.e(TAG, "ACCESS_COARSE_LOCATION=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION));
        }
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
}


/*
    working example of Location Updates
    private void initLocationUpdates() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.i(TAG, "onLocationChanged=" + location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i(TAG, "onStatusChanged=" + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i(TAG, "onProviderEnabled=" + provider);
                Toast.makeText(getApplicationContext(), "GPS is enabled.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.e(TAG, "onProviderDisabled=" + provider);
                Toast.makeText(getApplicationContext(), "GPS is disabled. Please turn it on.", Toast.LENGTH_LONG).show();
            }
        };

// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "GPS: permissions denied");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
*/

