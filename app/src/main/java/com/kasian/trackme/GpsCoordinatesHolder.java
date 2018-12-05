package com.kasian.trackme;

import java.util.concurrent.ConcurrentLinkedQueue;

public class GpsCoordinatesHolder {
    private ConcurrentLinkedQueue<Coordinate> coordinateHolder = new ConcurrentLinkedQueue<>();

    private static final GpsCoordinatesHolder instance = new GpsCoordinatesHolder();

    private GpsCoordinatesHolder() {
    }

    public static GpsCoordinatesHolder getInstance() {
        return instance;
    }

    public synchronized void add(Coordinate coordinate) {
        coordinateHolder.add(coordinate);
    }

    public synchronized Coordinate peek() {
        return coordinateHolder.peek();
    }

    public synchronized Coordinate poll() {
        return coordinateHolder.poll();
    }

    public synchronized boolean isEmpty() {
        return coordinateHolder.isEmpty();
    }
}
