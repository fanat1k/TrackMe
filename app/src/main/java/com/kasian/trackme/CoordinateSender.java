package com.kasian.trackme;

import android.support.annotation.NonNull;
import android.util.Log;

import com.kasian.trackme.data.Coordinate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CoordinateSender {
    private static final String TAG = "TrackMe:CoordinateSender";

    public int send(Coordinate coordinate) throws IOException, JSONException {

        URL url = new URL (CoordinateServerInfo.getInstance().getCoordinateServer());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", getAuthorizationProperty());
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        JSONObject jsonParam = getJsonObject(coordinate);
        Log.i(TAG, "send JSON:" + jsonParam.toString());

        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(jsonParam.toString());

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

    private static String getAuthorizationProperty() {
        CoordinateServerInfo serverInfo = CoordinateServerInfo.getInstance();
        return "Basic "
                + Base64.getEncoder()
                .encodeToString((serverInfo.getUser() + ":" + serverInfo.getPassword())
                        .getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    private JSONObject getJsonObject(Coordinate coordinate) throws JSONException {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("timestamp", coordinate.getTimestamp());
        jsonParam.put("latitude", coordinate.getLatitude());
        jsonParam.put("longitude", coordinate.getLongitude());
        return jsonParam;
    }
}
