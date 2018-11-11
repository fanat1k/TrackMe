package com.kasian.trackme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static boolean locationRequestPermissions = false;

    private static final int LOCATION_REQUEST_PERMISSION_CODE = 100;

    private static final String TAG = "GPSTracker";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        if (locationRequestPermissions) {
            startGpsTrackerService();
            //finishMainActivity();
        }
    }

    //TODO(kasian @2018-10-21): finish activity and left service working on background!
    private void finishMainActivity() {
        Log.i(TAG, "finish main activity");
        finish();
    }

    private void startGpsTrackerService() {
        Log.i(TAG, "startGpsTrackerService");

        Intent service = new Intent(this, GPSTrackerService.class);
        startService(service);

        //TODO(romanpe @2018-10-25): check later
        //ContextCompat.startForegroudService(this, service);
    }

    private void checkPermissions() {
        Log.i(TAG, "checkPermissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Request location permissions");

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_PERMISSION_CODE);
        } else {
            locationRequestPermissions = true;
            Log.i(TAG, "Location permissions are OK");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location permissions granted");
                    startGpsTrackerService();
                } else {
                    Log.i(TAG, "Location permissions declined");
                    Toast.makeText(getApplicationContext(), "Location permissions declined, exiting ...", Toast.LENGTH_LONG).show();
                }

                finishMainActivity();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
