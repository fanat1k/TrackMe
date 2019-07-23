package com.kasian.trackme;

import com.google.android.gms.location.LocationRequest;
import com.kasian.trackme.property.Properties;

import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static final String COORDINATES_PARAM = "coordinates";
    public static final String BATTERY_LEVEL_PARAM = "battery_level";
    public static final String LOCATION_UPDATES_ACTIVE_PARAM = "location_updates_active";
    public static final String BATTERY_IS_CHARGING_PARAM = "battery_is_charging";
    public static final String NOTIFICATION_CHANNEL_ID = "track_me_channel_id";

    // TODO: 23.07.2019 test coverage
    static long calculateStartDelay() {
        Date startTime = getRunTime(Properties.startTrackingTime);
        Date stopTime = getRunTime(Properties.stopTrackingTime);
        Date currentTime = Calendar.getInstance().getTime();

        long delay = 0;
        if (currentTime.before(startTime)) {
            delay = startTime.getTime() - currentTime.getTime();
        } else if (currentTime.after(stopTime)){
            Date nextDate = addOneDay(startTime);
            return nextDate.getTime() - currentTime.getTime();
        }
        return delay;
    }

    // TODO: 23.07.2019 test coverage
    static long calculateStopDelay() {
        Date stopTime = getRunTime(Properties.stopTrackingTime);
        Date currentTime = Calendar.getInstance().getTime();

        if (currentTime.before(stopTime)) {
            return stopTime.getTime() - currentTime.getTime();
        } else {
            Date nextDate = addOneDay(stopTime);
            return nextDate.getTime() - currentTime.getTime();
        }
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Properties.locationRequestUpdateIntervalMillis);
        locationRequest.setFastestInterval(Properties.locationRequestFastestIntervalMillis);
        return locationRequest;
    }

    private static Date addOneDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    private static Date getRunTime(int runHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, runHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }
}
