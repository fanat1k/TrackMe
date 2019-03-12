package com.kasian.trackme;

import java.text.SimpleDateFormat;
import java.util.Locale;

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

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDate() {
        return simpleDateFormat.format(timestamp);
    }

    public String getTime() {
        return simpleTimeFormat.format(timestamp);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
