package com.kasian.trackme;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kasian.trackme.property.Properties;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TrackMe:MainActivity";

    private GPSTrackerService gpsService;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Properties.init(this);
        Log.i(TAG, Properties.print());

        if (isGpsEnabled()) {
            checkPermissionsAndStartGpsService();
        }
    }

    private void startGpsTrackerService() {
        Log.i(TAG, "startGpsTrackerService");
        Intent service = new Intent(this, GPSTrackerService.class);

        if (!isServiceRunning(GPSTrackerService.class)) {
            startForegroundService(service);

            //to make gps location work in background
            startServiceConnector();
            this.getApplication().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void startServiceConnector() {
        serviceConnection = new ServiceConnection() {
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

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Toast.makeText(getApplicationContext(), "TrackMe Service is already running now.", Toast.LENGTH_LONG).show();
                return true;
            }
        }
        Toast.makeText(getApplicationContext(), "TrackMe Service is not running now. Will be started",
                Toast.LENGTH_LONG).show();
        return false;
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
                        Log.w(TAG, "Location permissions are disabled.");
                        if (response.isPermanentlyDenied()) {
                            Toast.makeText(getApplicationContext(),
                                    "Location permissions are disabled. Please turn it on.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private boolean isGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            Log.e(TAG, "LocationManager is null. Can't check if GPS is enabled.");
            return true;
        }

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            // notify user
            new AlertDialog.Builder(this)
                    .setMessage(R.string.gps_disabled_message)
                    .setPositiveButton(R.string.gps_open_settings_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            finishMainActivity();
                        }
                    })
                    .show();
            return false;
        }
    }

    // Finish main activity and let service working in background
    private void finishMainActivity() {
        Log.i(TAG, "Finish main activity.");
        finish();
    }
}
