package com.kasian.trackme;

public class Coordinate {
    private final long timestamp;
    private final double latitude;
    private final double longitude;
    private final String date;
    private final String time;

    public Coordinate(long timestamp, double latitude, double longitude, String date, String time) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.time = time;
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
        return date;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
