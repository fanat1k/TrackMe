package com.kasian.trackme;

import com.google.android.gms.location.LocationRequest;

import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static final int START_TRACKING_TIME = 8;     // 8AM
    public static final int STOP_TRACKING_TIME = 21;     // 9PM

    public static final int SCHEDULE_TRACKING_PERIOD = 60*60*24*1000;   // 24 hours
    public static final long COORDINATE_LIVE_TIME = 60*60*24*1000;      // 24 hours

    public static final String COORDINATES_PARAM = "coordinates";
    public static final String BATTERY_LEVEL_PARAM = "battery_level";
    public static final String BATTERY_IS_CHARGING_PARAM = "battery_is_charging";
    public static final String NOTIFICATION_CHANNEL_ID = "track_me_channel_id";
    public static final String DELIMITER = ";";

    private static final long LOCATION_REQUEST_UPDATE_INTERVAL = 10 * 1000;     // 10 secs
    private static final long LOCATION_REQUEST_FASTEST_INTERVAL = 5 * 1000;     //  5 secs

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
}
