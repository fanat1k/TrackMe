package com.kasian.trackme;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.kasian.trackme.coordinate.CoordinateServerInfoHolder;
import com.kasian.trackme.data.BatteryInfo;
import com.kasian.trackme.data.CoordinateServerProperty;
import com.kasian.trackme.data.HealthCheck;
import com.kasian.trackme.property.CoordinateServerInfoManagerImpl;
import com.kasian.trackme.property.Properties;
import com.kasian.trackme.service.GPSTrackerService;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ServiceActivity extends Activity {
    private GPSTrackerService mService;
    private ServiceConnection mConnection;
    private static boolean mBound = false;
    private static final Logger LOG = ALogger.getLogger(ServiceActivity.class);

    @Override
    protected void onStart() {
        super.onStart();
        LOG.info("onStart");

        // Coordinate server, user, password
        String server = getIntent().getStringExtra(Utils.PARAM_COORDINATE_SERVER);
        String user = getIntent().getStringExtra(Utils.PARAM_USER);
        String password = getIntent().getStringExtra(Utils.PARAM_PASSWORD);
        String userId = getIntent().getStringExtra(Utils.PARAM_USER_ID);

        if (server != null && user != null && password != null && userId != null) {
            setCoordinateServerInfo(server, user, password, userId);
            finish();
        }

        // Location requesting start_time and stop_time
        String startTime = getIntent().getStringExtra(Utils.START_TIME);
        String stopTime = getIntent().getStringExtra(Utils.STOP_TIME);
        if (startTime != null) {
            setTime(startTime, true);
            finish();
        }
        if (stopTime != null) {
            setTime(stopTime, false);
            finish();
        }

        // Battary level info
        if (getIntent().getStringExtra(Utils.PARAM_BATTERY_LEVEL) != null) {
            getAndSendBackBatteryInfo();
            finish();
        }

        // Last logs
        String logs = getIntent().getStringExtra(Utils.PARAM_LOGS);
        if (logs != null) {
            getAndSendBackLogs(getIntent().getStringExtra(Utils.PARAM_LOG_LINES));
            finish();
        }

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                LOG.info("onServiceConnected");
                GPSTrackerService.LocationServiceBinder binder = (GPSTrackerService.LocationServiceBinder) service;
                mService = binder.getService();
                mBound = true;

                String getHealthCheck = getIntent().getStringExtra(Utils.PARAM_HEALTHCHECK);
                if (getHealthCheck != null) {
                    getAndSendBackHealthcheck();
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
        LOG.info("onStop");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private void setCoordinateServerInfo(String server, String user, String password, String userId) {
        LOG.info("setCoordinateServerInfo:server=" + server + ";user=" + user + ";userId=" + userId);

        CoordinateServerProperty coordinateServerProperty = CoordinateServerProperty.builder()
                .address(server)
                .user(user)
                .password(password)
                .userId(userId)
                .build();
        CoordinateServerInfoHolder.getInstance().setProperty(coordinateServerProperty);
        new CoordinateServerInfoManagerImpl(getApplicationContext()).setCoordinateServerProperty(coordinateServerProperty);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.PARAM_RESPONSE, "Coordinate server has been set");
        setResult(Activity.RESULT_OK, returnIntent);
    }

    private void setTime(String time, boolean isStartTime) {
        String kindOfTime = isStartTime ? "start_time" : "stop_time";
        LOG.info("set " + kindOfTime + "=" + time);
        LocalTime localTime = Utils.getLocalTimeFromHHMM(time);
        if (isStartTime) {
            Properties.startTrackingHour = localTime.getHour();
            Properties.startTrackingMin = localTime.getMinute();
        } else {
            Properties.stopTrackingHour = localTime.getHour();
            Properties.stopTrackingMin = localTime.getMinute();
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.PARAM_RESPONSE, kindOfTime + " has been set");
        setResult(Activity.RESULT_OK, returnIntent);
    }

    private void getAndSendBackHealthcheck() {
        boolean status = mService.getLocationUpdateStatus();
        String lastLocationUpdateTime = mService.getLastLocationUpdateTime();
        String lastLocationSendTime = mService.getLastLocationSendTime();
        Integer coordinateCacheSize = mService.getCoordinateCacheSize();
        CoordinateServerProperty coordinateServerInfo = mService.getCoordinateServerInfo();

        HealthCheck healthCheck = HealthCheck.builder()
                .status(HealthCheck.Status.parse(status))
                .locationLastUpdateTime(lastLocationUpdateTime)
                .locationLastSendTime(lastLocationSendTime)
                .coordinateCacheSize(coordinateCacheSize)
                .coordinateServerInfo(coordinateServerInfo)
                .build();
        LOG.info("getAndSendBackHealthcheck=" + healthCheck);

        Intent returnIntent = new Intent();
        String healthCheckString = getJson(healthCheck).toString()
                .replace("\\/", "/")
                .replace("\\\\", "\\");

        returnIntent.putExtra(Utils.PARAM_HEALTHCHECK, healthCheckString);
        setResult(Activity.RESULT_OK, returnIntent);
    }

    @NonNull
    // TODO: 02.02.2020 use Jackson
    private JSONObject getJson(HealthCheck healthCheck) {
        JSONObject jsonObject = new JSONObject();
        try {
            CoordinateServerProperty coordinateServerInfo = healthCheck.getCoordinateServerInfo();
            JSONObject coordinateServerInfoJson = new JSONObject();
            coordinateServerInfoJson.put("address", coordinateServerInfo.getAddress());
            coordinateServerInfoJson.put("user", coordinateServerInfo.getUser());
            coordinateServerInfoJson.put("password", coordinateServerInfo.getPassword());
            return jsonObject
                    .put("date", Utils.getDateFormatted(LocalDateTime.now()))
                    .put("status", healthCheck.getStatus())
                    .put("locationLastUpdateTime", healthCheck.getLocationLastUpdateTime())
                    .put("locationLastSendTime", healthCheck.getLocationLastSendTime())
                    .put("coordinateCacheSize", healthCheck.getCoordinateCacheSize())
                    .put("coordinateServerInfo", coordinateServerInfoJson);
        } catch (JSONException e) {
            LOG.error("Can not create json object from healthcheck=" + healthCheck);
        }
        return jsonObject;
    }

    private void getAndSendBackBatteryInfo() {
        BatteryInfo batteryInfo = getBatteryInfo();
        LOG.info("getAndSendBackBatteryInfo=" + batteryInfo);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.PARAM_BATTERY_LEVEL, String.valueOf(batteryInfo.getBatteryLevel()));
        returnIntent.putExtra(Utils.PARAM_BATTERY_IS_CHARGING, String.valueOf(batteryInfo.isCharging()));
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
            LOG.info("batteryInfo=" + batteryInfo);
        } else {
            LOG.error("Can not get battery info");
        }
        return batteryInfo;
    }

    private void getAndSendBackLogs(String numLines) {
        Integer lines = numLines == null ? null : Integer.valueOf(numLines);
        if (lines == null) {
            lines = Integer.MAX_VALUE;
        }
        String logs = Utils.arrayToString(Utils.readFile(Utils.LOG_FILE_NAME, lines));

        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.PARAM_LOGS, logs);
        setResult(Activity.RESULT_OK, returnIntent);
    }

}
