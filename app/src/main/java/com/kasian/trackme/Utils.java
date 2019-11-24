package com.kasian.trackme;

import com.google.android.gms.location.LocationRequest;
import com.kasian.trackme.property.Properties;

import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static final int HTTP_OK = 200;
    public static final String PARAM_RESPONSE_OK = "response_ok";
    public static final String PARAM_COORDINATE_SERVER = "server";
    public static final String PARAM_USER = "user";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_BATTERY_LEVEL = "battery_level";
    public static final String PARAM_LOCATION_UPDATES_ACTIVE = "location_updates_active";
    public static final String PARAM_BATTERY_IS_CHARGING = "battery_is_charging";
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
