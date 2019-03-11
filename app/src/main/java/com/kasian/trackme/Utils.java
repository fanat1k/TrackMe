package com.kasian.trackme;

import com.google.android.gms.location.LocationRequest;

import java.util.Calendar;
import java.util.Date;

public class Utils {
    // TODO: 11.03.2019 move all properties to properties-file?
    public static final int START_TIME = 1;        // 8AM      // TODO: 11.03.2019
    public static final int STOP_TIME = 23;        // 9PM      // TODO: 02.03.2019

    public static final int PERIOD = 60*60*24*1000;                // 24 hours
    public static final long COORDINATE_LIVE_TIME = 60*60*24*1000; // 24 hours

    public static final String NOTIFICATION_CHANNEL_ID = "track_me_channel_id";
    public static final String DELIMITER = ";";

    private static final long LOCATION_REQUEST_UPDATE_INTERVAL = 30 * 1000;     // 30 secs
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 10 * 1000;    // 10 secs


    public static Date addOneDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public static Date getRunTime(int runHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, runHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();

        //TODO: check if WiFi is used in case of PRIORITY_HIGH_ACCURACY
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationRequest.setInterval(LOCATION_REQUEST_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);

        return locationRequest;
    }

/*
    private void initProperties(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
