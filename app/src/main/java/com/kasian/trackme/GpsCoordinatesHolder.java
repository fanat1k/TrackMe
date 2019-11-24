package com.kasian.trackme;

import com.kasian.trackme.data.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GpsCoordinatesHolder {
    private ConcurrentLinkedQueue<Coordinate> coordinateHolder = new ConcurrentLinkedQueue<>();

    private static final GpsCoordinatesHolder instance = new GpsCoordinatesHolder();

    private GpsCoordinatesHolder() {
    }

    public static GpsCoordinatesHolder getInstance() {
        return instance;
    }

    public void add(Coordinate coordinate) {
        coordinateHolder.add(coordinate);
    }

    public Coordinate peek() {
        return coordinateHolder.peek();
    }

    public Coordinate poll() {
        return coordinateHolder.poll();
    }

    public boolean isEmpty() {
        return coordinateHolder.isEmpty();
    }

    // TODO: 24.11.2019 send list of coordinates instead one-by-one
    public List<Coordinate> getAllCoordinates() {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        if (isEmpty()) {
            return coordinates;
        }

        while (!coordinateHolder.isEmpty()) {
            Coordinate coordinate = coordinateHolder.poll();
            if (coordinate != null) {
                coordinates.add(coordinate);
            }
        }

        return coordinates;
    }
}
