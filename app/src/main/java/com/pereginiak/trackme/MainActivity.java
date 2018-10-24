package com.pereginiak.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GPSTracker";
    private static final int PERMISSION_REQUEST_CODE = 1000;

    private static long UPDATE_INTERVAL = 10 * 1000;
    private static long FASTEST_INTERVAL = 2 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO(kasian @2018-09-30): is this a right place?
        startGpsTrackerService();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //TODO(kasian @2018-10-21): finish activity and left service working on background!
        Log.i(TAG, "finish main activity");
        finish();
    }

    private void startGpsTrackerService() {
        Intent service = new Intent(this, GPSTrackerService.class);
        //ContextCompat.startForegroudService(this, service);
        startService(service);
    }
}
