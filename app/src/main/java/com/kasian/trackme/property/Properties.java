package com.kasian.trackme.property;

import android.content.Context;

public class Properties {
    public static int startTrackingTime;
    public static int stopTrackingTime;
    public static long locationRequestUpdateIntervalMillis;
    public static long locationRequestFastestIntervalMillis;
    public static long coordinateLiveTime;
    public static long pingTimeoutMillis;
    public static long updateLocationCheckerMillis;
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
        pingTimeoutMillis = Long.valueOf(propertyReader.getProperty("PING_TIMEOUT_MILLIS"));
        updateLocationCheckerMillis = Long.valueOf(propertyReader.getProperty("UPDATE_LOCATION_CHECKER_MILLIS"));

        delimiter = propertyReader.getProperty("DELIMITER");
    }

    public static String print() {
        return "\nstartTrackingTime" + "=" + startTrackingTime +
                "\nstopTrackingTime" + "=" + stopTrackingTime +
                "\nlocationRequestUpdateIntervalMillis" + "=" + locationRequestUpdateIntervalMillis +
                "\nlocationRequestFastestIntervalMillis" + "=" + locationRequestFastestIntervalMillis +
                "\ncoordinateLiveTime" + "=" + coordinateLiveTime +
                "\npingTimeoutMillis" + "=" + pingTimeoutMillis +
                "\nupdateLocationCheckerMillis" + "=" + updateLocationCheckerMillis +
                "\ndelimiter" + "=" + delimiter;
    }
}
