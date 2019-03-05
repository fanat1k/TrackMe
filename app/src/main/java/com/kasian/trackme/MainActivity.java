package com.kasian.trackme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static boolean locationRequestPermissions = false;
    private static final int LOCATION_REQUEST_PERMISSION_CODE = 20190305;
    private static final String TAG = "GPSTracker:MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        if (locationRequestPermissions) {
            startGpsTrackerService();
            finishMainActivity();
        }
    }

    private void startGpsTrackerService() {
        Log.i(TAG, "startGpsTrackerService");

        Intent service = new Intent(this, GPSTrackerService.class);
        startService(service);

        //TODO(romanpe @2018-10-25): check later
        //ContextCompat.startForegroudService(this, service);
    }

    // Finish main activity and left service working in background
    private void finishMainActivity() {
        Log.i(TAG, "Finish main activity.");
        finish();
    }

    private void checkPermissions() {
        Log.i(TAG, "checkPermissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions disabled. Requesting location permissions.");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_PERMISSION_CODE);
        } else {
            Log.i(TAG, "Location permissions enabled.");
            locationRequestPermissions = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");
        Log.i(TAG, "requestCode=" + requestCode + ";permissions=" + permissions + ";grantResults=" + grantResults);

        switch (requestCode) {
            case LOCATION_REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result array is empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location permissions granted.");
                    locationRequestPermissions = true;
                    startGpsTrackerService();
                } else {
                    Log.i(TAG, "Location permissions declined.");
                    Toast.makeText(getApplicationContext(), "Location permissions declined, exiting ...", Toast.LENGTH_LONG).show();
                }

                finishMainActivity();
            }
        }
    }
}
