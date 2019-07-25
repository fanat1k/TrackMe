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

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Properties.locationRequestUpdateIntervalMillis);
        locationRequest.setFastestInterval(Properties.locationRequestFastestIntervalMillis);
        return locationRequest;
    }

    public static Date getTime(int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }
}
