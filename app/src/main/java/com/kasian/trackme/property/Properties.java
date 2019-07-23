package com.kasian.trackme.property;

import android.content.Context;

public class Properties {
    public static int startTrackingTime;
    public static int stopTrackingTime;
    public static long locationRequestUpdateIntervalMillis;
    public static long locationRequestFastestIntervalMillis;
    public static long coordinateLiveTime;
    public static String delimiter;

    public static void init(Context context) {
        PropertyReader propertyReader = new PropertyReader(context);

        startTrackingTime = Integer.valueOf(propertyReader.getProperty("START_TRACKING_TIME"));
        stopTrackingTime = Integer.valueOf(propertyReader.getProperty("STOP_TRACKING_TIME"));

        locationRequestUpdateIntervalMillis = Long.valueOf(
                propertyReader.getProperty("LOCATION_REQUEST_UPDATE_INTERVAL_MILLIS"));
        locationRequestFastestIntervalMillis = Long.valueOf(
                propertyReader.getProperty("LOCATION_REQUEST_FASTEST_INTERVAL_MILLIS"));

        coordinateLiveTime = Long.valueOf(propertyReader.getProperty("COORDINATE_LIVE_TIME"));

        delimiter = propertyReader.getProperty("DELIMITER");
    }
}
