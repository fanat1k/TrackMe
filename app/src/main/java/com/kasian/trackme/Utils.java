package com.kasian.trackme;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static final int HTTP_OK = 200;
    public static final String PARAM_RESPONSE = "response";
    public static final String PARAM_COORDINATE_SERVER = "server";
    public static final String PARAM_USER = "user";
    public static final String PARAM_PASSWORD = "password";
    public static final String START_TIME = "start_time";
    public static final String STOP_TIME = "stop_time";
    public static final String PARAM_BATTERY_LEVEL = "battery_level";
    public static final String PARAM_HEALTHCHECK = "healthcheck";
    public static final String PARAM_BATTERY_IS_CHARGING = "battery_is_charging";
    public static final String NOTIFICATION_CHANNEL_ID = "track_me_channel_id";
    public static final int NOTIFICATION_CHANNEL_ID_INT = 1912281636;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

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

    public static LocalTime getLocalTimeFromHHMM(String time) {
        String[] t = time.split(":");
        return LocalTime.of(Integer.valueOf(t[0]), Integer.valueOf(t[1]));
    }

    public static String getDateFormatted(LocalDateTime date) {
        return dateTimeFormatter.format(date);
    }
}
