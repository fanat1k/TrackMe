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
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kasian.trackme.property.Properties;
import com.kasian.trackme.service.GPSTrackerService;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GPSTrackerService gpsTrackerService;
    private ServiceConnection serviceConnection;
    private static final String TAG = "TrackMe:MainActivity";
    private Logger LOG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isGpsEnabled()) {
            checkPermissionsAndStartGpsService();
        }
    }

    private void startGpsTrackerService() {
        LOG.info("startGpsTrackerService");
        Intent service = new Intent(this, GPSTrackerService.class);

        if (!isServiceRunning(GPSTrackerService.class)) {
            startForegroundService(service);
            LOG.info("Started foreground service.");

            //To make gps location work in background
            startServiceConnector();
            this.getApplication().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
            LOG.info("Service is binded (dependency between app and service)");
        }
    }

    private void startServiceConnector() {
        LOG.info("startServiceConnector");
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                String name = className.getClassName();
                if (name.endsWith("GPSTrackerService")) {
                    gpsTrackerService = ((GPSTrackerService.LocationServiceBinder) service).getService();
                    LOG.info("gpsTrackerService initiated successfully.");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                if (className.getClassName().equals("GPSTrackerService")) {
                    gpsTrackerService = null;
                }
            }
        };
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                String message = "TrackMe is already running now.";
                LOG.warn(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                return true;
            }
        }
        String message = "TrackMe is not running now. Starting...";
        LOG.info(message);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        return false;
    }

    private void checkPermissionsAndStartGpsService() {
        Log.i(TAG, "checkPermissionsAndStartGpsService");

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Log.i(TAG, "All permissions are granted.");
                            initLogger();
                            initProperties();
                            startGpsTrackerService();
                            finishMainActivity();
                        }

                        // check if any permission is denied
                        List<PermissionDeniedResponse> deniedPermissionResponses = report.getDeniedPermissionResponses();
                        if (deniedPermissionResponses.size() > 0) {
                            List<String> deniedPermissions = new ArrayList<>();
                            for (PermissionDeniedResponse deniedPermissionResponse : deniedPermissionResponses) {
                                deniedPermissions.add(deniedPermissionResponse.getPermissionName());
                            }

                            showAlertAndExit("Permissions denied",
                                    "Please grant the following permissions and restart application: " + deniedPermissions);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showAlertAndExit(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishMainActivity();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private boolean isGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            LOG.error("LocationManager is null. Can't check if GPS is enabled.");
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

    private void initProperties() {
        Properties.init(this);
        LOG.info(Properties.print());
    }

    private void initLogger() {
        LOG = ALogger.getLogger(MainActivity.class);
    }

    // Finish main activity and let service working in background
    private void finishMainActivity() {
        Log.i(TAG,"Finish main activity.");
        finish();
    }
}
