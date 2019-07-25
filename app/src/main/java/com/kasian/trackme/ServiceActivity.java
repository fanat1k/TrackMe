package com.kasian.trackme;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import com.kasian.trackme.data.BatteryInfo;

public class ServiceActivity extends Activity {
    private GPSTrackerService mService;
    private ServiceConnection mConnection;
    private static boolean mBound = false;

    private static final String TAG = "TrackMe:ServiceActivity";

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart");

        String shouldGetBatteryLevel = getIntent().getStringExtra(Utils.BATTERY_LEVEL_PARAM);
        Log.i(TAG, "get_battery_level=" + shouldGetBatteryLevel);
        if (shouldGetBatteryLevel != null) {
            getAndSendBackBatteryInfo();
            finish();
        }

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(TAG, "onServiceConnected");
                GPSTrackerService.LocationServiceBinder binder = (GPSTrackerService.LocationServiceBinder) service;
                mService = binder.getService();
                mBound = true;

                String shouldGetLocationUpdatesStatus = getIntent().getStringExtra(Utils.LOCATION_UPDATES_ACTIVE_PARAM);
                if (shouldGetLocationUpdatesStatus == null) {
                    getAndSendBackCoordinates();
                } else {
                    getAndSendBackLocationUpdatesStatus();
                }
                finish();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };

        Intent intent = new Intent(this, GPSTrackerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void getAndSendBackCoordinates() {
        String coordinates = mService.getAllCoordinates();
        Log.i(TAG, "getAndSendBackCoordinates");

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.COORDINATES_PARAM, coordinates);
        setResult(Activity.RESULT_OK, returnIntent);
    }

    private void getAndSendBackBatteryInfo() {
        BatteryInfo batteryInfo = getBatteryInfo();
        Log.i(TAG, "getAndSendBackBatteryInfo=" + batteryInfo);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.BATTERY_LEVEL_PARAM, String.valueOf(batteryInfo.getBatteryLevel()));
        returnIntent.putExtra(Utils.BATTERY_IS_CHARGING_PARAM, String.valueOf(batteryInfo.isCharging()));
        setResult(Activity.RESULT_OK, returnIntent);
    }

    private void getAndSendBackLocationUpdatesStatus() {
        boolean status = mService.getLocationUpdatesStatus();
        Log.i(TAG, "getAndSendBackLocationUpdatesStatus=" + status);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.LOCATION_UPDATES_ACTIVE_PARAM, String.valueOf(status));
        setResult(Activity.RESULT_OK, returnIntent);
    }

    private BatteryInfo getBatteryInfo() {
        BatteryInfo batteryInfo = new BatteryInfo();
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            // BATTERY_STATUS_FULL (5) will be shown as not charing, it's ok so far
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

            batteryInfo.setBatteryLevel(level);
            batteryInfo.setCharging(isCharging);
            Log.i(TAG, "batteryInfo=" + batteryInfo);
        } else {
            Log.e(TAG, "Can not get battery info");
        }
        return batteryInfo;
    }
}
