package com.kasian.trackme.coordinate;

import com.kasian.trackme.data.Coordinate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoordinateHolder {
    private List<Coordinate> coordinateHolder = Collections.synchronizedList(new ArrayList<Coordinate>());

    private static final CoordinateHolder instance = new CoordinateHolder();

    private CoordinateHolder() {
    }

    public static CoordinateHolder getInstance() {
        return instance;
    }

    public void add(Coordinate coordinate) {
        coordinateHolder.add(coordinate);
    }

    public List<Coordinate> getAll() {
        return coordinateHolder;
    }

    public void clear() {
        coordinateHolder.clear();
    }

    public void removeAll(List<Coordinate> coordinates) {
        coordinateHolder.removeAll(coordinates);
    }

    public boolean isEmpty() {
        return coordinateHolder.isEmpty();
    }

    public int size() {
        return coordinateHolder.size();
    }
}
