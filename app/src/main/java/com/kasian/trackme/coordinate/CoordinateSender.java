package com.kasian.trackme.coordinate;

import com.kasian.trackme.data.Coordinate;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

interface CoordinateSender {
    int send(List<Coordinate> coordinates) throws IOException, JSONException;
}
