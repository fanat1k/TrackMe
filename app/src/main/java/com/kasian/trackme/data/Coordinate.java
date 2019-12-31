package com.kasian.trackme.data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Coordinate {
    private final LocalDateTime  date;
    private final double latitude;
    private final double longitude;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public Coordinate(double latitude, double longitude) {
        this.date = LocalDateTime.now();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDateFormatted() {
        return dateTimeFormatter.format(date);
    }

    public long getTimestamp() {
        return Instant.from(date).toEpochMilli();
    }
}
