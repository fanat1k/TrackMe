package com.kasian.trackme;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GPSTracker:MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissionsAndStartGpsService();
    }

    private void startGpsTrackerService() {
        Log.i(TAG, "startGpsTrackerService");
        Intent service = new Intent(this, GPSTrackerService.class);

        //startService(service);
        startForegroundService(service);

        //to make gps location work in background
        this.getApplication().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // Finish main activity and left service working in background
    private void finishMainActivity() {
        Log.i(TAG, "Finish main activity.");
        finish();
    }

    private void checkPermissionsAndStartGpsService() {
        Log.i(TAG, "checkPermissionsAndStartGpsService");

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Log.i(TAG, "Location permissions enabled.");
                        startGpsTrackerService();
                        finishMainActivity();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Log.w(TAG, "Location permissions disabled. Requesting location permissions.");
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        startActivity(intent);
    }

    // TODO: 11.03.2019 later add checking (need to tune this method)
    private void isGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gps_enabled) {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage(R.string.gps_disabled)
                    .setPositiveButton(R.string.gps_disabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }


    private GPSTrackerService gpsService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("GPSTrackerService")) {
                gpsService = ((GPSTrackerService.LocationServiceBinder) service).getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("GPSTrackerService")) {
                gpsService = null;
            }
        }
    };
}
