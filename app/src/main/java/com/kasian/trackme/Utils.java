package com.kasian.trackme;

import android.os.Environment;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Utils {
    public static final int HTTP_OK = 200;
    public static final String PARAM_RESPONSE = "response";
    public static final String PARAM_COORDINATE_SERVER = "server";
    public static final String PARAM_USER = "user";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USER_ID = "login";
    public static final String START_TIME = "start_time";
    public static final String STOP_TIME = "stop_time";
    public static final String PARAM_BATTERY_LEVEL = "battery_level";
    public static final String PARAM_HEALTHCHECK = "healthcheck";
    public static final String PARAM_LOGS = "logs";
    public static final String PARAM_LOG_LINES = "log_lines";
    public static final String PARAM_BATTERY_IS_CHARGING = "battery_is_charging";
    public static final String NOTIFICATION_CHANNEL_ID = "track_me_channel_id";
    public static final int NOTIFICATION_CHANNEL_ID_INT = 1912281636;
    public static final String LOG_FILE_NAME = Environment.getExternalStorageDirectory().toString() + File.separator + "trackme.log";
    private static final Logger LOG = ALogger.getLogger(Utils.class);

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

    public static List<String> readFile(String fileName, Integer lineNumber) {
        List<String> lines = new ArrayList<>();
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(fileName), StandardCharsets.UTF_8)){
            String line;
            while ((line = reader.readLine()) != null && lines.size() < lineNumber) {
                lines.add(line);
            }
        } catch (IOException e) {
            LOG.error("Can not read file: " + LOG_FILE_NAME, e);
        }
        Collections.reverse(lines);
        return lines;
    }

    public static String arrayToString(List<String> lines) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
