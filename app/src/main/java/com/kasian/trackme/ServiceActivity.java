package com.kasian.trackme;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ServiceActivity extends Activity {
    private GPSTrackerService mService;
    private ServiceConnection mConnection;
    private static boolean mBound = false;

    private static final String TAG = "GPSTracker:ServiceActivity";

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart");

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(TAG, "onServiceConnected");
                GPSTrackerService.LocationServiceBinder binder = (GPSTrackerService.LocationServiceBinder) service;
                mService = binder.getService();
                mBound = true;

                getAndSendBackCoordinates();
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
        Log.i(TAG, "getAndSendBackCoordinates");

        Intent returnIntent = new Intent();

        String coordinates = mService.getAllCoordinates();

        returnIntent.putExtra(Utils.COORDINATES_PARAM, coordinates);
        setResult(Activity.RESULT_OK, returnIntent);
    }
}
