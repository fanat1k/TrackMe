package com.kasian.trackme.property;

import android.content.Context;

public class Properties {
    public static int startTrackingHour;
    public static int startTrackingMin;
    public static int stopTrackingHour;
    public static int stopTrackingMin;
    public static int locationInterval;
    public static int locationDistance;
    public static long coordinateLiveTimeMillis;
    public static long cleanCoordinatesDelayHour;
    public static long cleanCoordinatesPeriodHour;
    public static long checkLivenessPeriodMin;
    public static long updateLocationCheckerMin;

    public static void init(Context context) {
        PropertyReader propertyReader = new PropertyReader(context);

        startTrackingHour = Integer.valueOf(propertyReader.getProperty("START_TRACKING_HOUR"));
        startTrackingMin = Integer.valueOf(propertyReader.getProperty("START_TRACKING_MIN"));
        stopTrackingHour = Integer.valueOf(propertyReader.getProperty("STOP_TRACKING_HOUR"));
        stopTrackingMin = Integer.valueOf(propertyReader.getProperty("STOP_TRACKING_MIN"));

        locationInterval = Integer.valueOf(propertyReader.getProperty("LOCATION_INTERVAL"));
        locationDistance = Integer.valueOf(propertyReader.getProperty("LOCATION_DISTANCE"));

        coordinateLiveTimeMillis = Long.valueOf(propertyReader.getProperty("COORDINATE_LIVE_TIME_MILLIS"));
        cleanCoordinatesDelayHour = Long.valueOf(propertyReader.getProperty("CLEAN_COORDINATES_DELAY_HOUR"));
        cleanCoordinatesPeriodHour = Long.valueOf(propertyReader.getProperty("CLEAN_COORDINATES_PERIOD_HOUR"));

        updateLocationCheckerMin = Long.valueOf(propertyReader.getProperty("UPDATE_LOCATION_CHECKER_MIN"));
        checkLivenessPeriodMin = Long.valueOf(propertyReader.getProperty("CHECK_LIVENESS_PERIOD_MIN"));
    }

    public static String print() {
        return "\nstartTrackingHour" + "=" + startTrackingHour +
                "\nstartTrackingMin" + "=" + startTrackingMin +
                "\nstopTrackingHour" + "=" + stopTrackingHour +
                "\nstopTrackingMin" + "=" + stopTrackingMin +
                "\nlocationInterval" + "=" + locationInterval +
                "\nlocationDistance" + "=" + locationDistance +
                "\ncoordinateLiveTimeMillis" + "=" + coordinateLiveTimeMillis +
                "\ncleanCoordinatesDelayHour" + "=" + cleanCoordinatesDelayHour +
                "\ncleanCoordinatesPeriodHour" + "=" + cleanCoordinatesPeriodHour +
                "\nupdateLocationCheckerMin" + "=" + updateLocationCheckerMin +
                "\ncheckLivenessPeriodMin" + "=" + checkLivenessPeriodMin;
    }
}
