package com.kasian.trackme.data;

import java.text.SimpleDateFormat;
import java.util.Locale;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Coordinate {
    private final long timestamp;
    private final double latitude;
    private final double longitude;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public Coordinate(double latitude, double longitude) {
        this.timestamp = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return simpleDateFormat.format(timestamp);
    }

    public String getTime() {
        return simpleTimeFormat.format(timestamp);
    }
}
