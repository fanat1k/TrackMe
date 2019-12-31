package com.kasian.trackme.property;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kasian.trackme.data.CoordinateServerProperty;

public class CoordinateServerInfoManagerImpl implements CoordinateServerInfoManager {
    private Context context;
    private static final String PROPERTY_SERVER = "address";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_PASSWORD = "password";
    private static final String TAG = "TrackMe:CoordinateServerInfoManagerImpl";

    public CoordinateServerInfoManagerImpl(Context context) {
        this.context = context;
    }

    @Override
    public CoordinateServerProperty getCoordinateServerProperty() {
        Log.i(TAG, "getCoordinateServerProperty");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String server = settings.getString(PROPERTY_SERVER, null);
        String user = settings.getString(PROPERTY_USER, null);
        String password = settings.getString(PROPERTY_PASSWORD, null);
        if (server != null && user != null && password != null) {
            return CoordinateServerProperty.builder()
                    .address(server)
                    .user(user)
                    .password(password)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public void setCoordinateServerProperty(CoordinateServerProperty serverProperty) {
        Log.i(TAG, "setCoordinateServerProperty, serverProperty=" + serverProperty);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("address", serverProperty.getAddress());
        edit.putString("user", serverProperty.getUser());
        edit.putString("password", serverProperty.getPassword());
        edit.apply();
    }
}
