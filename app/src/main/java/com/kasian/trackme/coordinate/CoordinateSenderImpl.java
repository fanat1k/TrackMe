package com.kasian.trackme.coordinate;

import android.support.annotation.NonNull;
import android.util.Log;

import com.kasian.trackme.Utils;
import com.kasian.trackme.data.Coordinate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class CoordinateSenderImpl implements CoordinateSender {
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;
    private static final int READ_TIMEOUT_MILLIS = 10000;
    private static final String TAG = "TrackMe:CoordinateSenderImpl";

    @Override
    public int send(List<Coordinate> coordinates) throws IOException, JSONException {
        URL url = new URL (CoordinateServerInfoHolder.getInstance().getServer());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        setHeaders(con);

        JSONArray coordinatesJson = getJson(coordinates);
        Log.i(TAG, "send JSON:" + coordinatesJson.toString());

        Writer os = new OutputStreamWriter(con.getOutputStream());
        os.write(coordinatesJson.toString());
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        Log.i(TAG, "responseCode=" + responseCode);
        if (responseCode != Utils.HTTP_OK) {
            Log.i(TAG, "responseMessage:" + con.getResponseMessage());
        }
        con.disconnect();

        return responseCode;
    }

    private void setHeaders(HttpURLConnection con) throws ProtocolException {
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        con.setReadTimeout(READ_TIMEOUT_MILLIS);   // Not sure if it's relevant to POST requests
        con.setRequestProperty("Authorization", getAuthorizationProperty());
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
    }

    private static String getAuthorizationProperty() {
        CoordinateServerInfoHolder serverInfo = CoordinateServerInfoHolder.getInstance();
        return "Basic "
                + Base64.getEncoder()
                .encodeToString((serverInfo.getUser() + ":" + serverInfo.getPassword())
                        .getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    private JSONArray getJson(List<Coordinate> coordinates) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Coordinate coordinate : coordinates) {
            jsonArray.put(new JSONObject()
                    .put("date", coordinate.getDateFormatted())
                    .put("latitude", coordinate.getLatitude())
                    .put("longitude", coordinate.getLongitude()));
        }
        return jsonArray;
    }
}
