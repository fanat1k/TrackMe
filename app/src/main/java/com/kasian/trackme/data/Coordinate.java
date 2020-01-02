package com.kasian.trackme.data;

import com.kasian.trackme.Utils;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Coordinate {
    private final LocalDateTime  date;
    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        this.date = LocalDateTime.now();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDateFormatted() {
        return Utils.getDateFormatted(date);
    }

    public long getTimestamp() {
        return Instant.from(date).toEpochMilli();
    }
}
