package com.kasian.trackme.property;

import android.content.Context;

public class Properties {
    public static int startTrackingHour;
    public static int startTrackingMin;
    public static int stopTrackingHour;
    public static int stopTrackingMin;
    public static long locationRequestUpdateIntervalMillis;
    public static long locationRequestFastestIntervalMillis;
    public static long coordinateLiveTime;
    public static long cleanCoordinatesDelayHour;
    public static long cleanCoordinatesPeriodHour;
    public static long checkLivenessPeriodMin;
    public static long updateLocationCheckerMin;
    public static String delimiter;

    public static void init(Context context) {
        PropertyReader propertyReader = new PropertyReader(context);

        startTrackingHour = Integer.valueOf(propertyReader.getProperty("START_TRACKING_HOUR"));
        startTrackingMin = Integer.valueOf(propertyReader.getProperty("START_TRACKING_MIN"));
        stopTrackingHour = Integer.valueOf(propertyReader.getProperty("STOP_TRACKING_HOUR"));
        stopTrackingMin = Integer.valueOf(propertyReader.getProperty("STOP_TRACKING_MIN"));

        locationRequestUpdateIntervalMillis = Long.valueOf(
                propertyReader.getProperty("LOCATION_REQUEST_UPDATE_INTERVAL_MILLIS"));
        locationRequestFastestIntervalMillis = Long.valueOf(
                propertyReader.getProperty("LOCATION_REQUEST_FASTEST_INTERVAL_MILLIS"));

        coordinateLiveTime = Long.valueOf(propertyReader.getProperty("COORDINATE_LIVE_TIME"));
        cleanCoordinatesDelayHour = Long.valueOf(propertyReader.getProperty("CLEAN_COORDINATES_DELAY_HOUR"));
        cleanCoordinatesPeriodHour = Long.valueOf(propertyReader.getProperty("CLEAN_COORDINATES_PERIOD_HOUR"));

        updateLocationCheckerMin = Long.valueOf(propertyReader.getProperty("UPDATE_LOCATION_CHECKER_MIN"));
        checkLivenessPeriodMin = Long.valueOf(propertyReader.getProperty("CHECK_LIVENESS_PERIOD_MIN"));

        delimiter = propertyReader.getProperty("DELIMITER");
    }

    public static String print() {
        return "\nstartTrackingHour" + "=" + startTrackingHour +
                "\nstartTrackingMin" + "=" + startTrackingMin +
                "\nstopTrackingHour" + "=" + stopTrackingHour +
                "\nstopTrackingMin" + "=" + stopTrackingMin +
                "\nlocationRequestUpdateIntervalMillis" + "=" + locationRequestUpdateIntervalMillis +
                "\nlocationRequestFastestIntervalMillis" + "=" + locationRequestFastestIntervalMillis +
                "\ncoordinateLiveTime" + "=" + coordinateLiveTime +
                "\ncleanCoordinatesDelayHour" + "=" + cleanCoordinatesDelayHour +
                "\ncleanCoordinatesPeriodHour" + "=" + cleanCoordinatesPeriodHour +
                "\nupdateLocationCheckerMin" + "=" + updateLocationCheckerMin +
                "\ncheckLivenessPeriodMin" + "=" + checkLivenessPeriodMin +
                "\ndelimiter" + "=" + delimiter;
    }
}
