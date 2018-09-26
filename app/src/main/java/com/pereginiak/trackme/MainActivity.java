package com.pereginiak.trackme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startService();

        finish();
    }

    private void startService() {

        Intent service = new Intent(this, GPSTracker.class);
        ContextCompat.startForegroundService(this, service);
        //startService(service);
    }
}
