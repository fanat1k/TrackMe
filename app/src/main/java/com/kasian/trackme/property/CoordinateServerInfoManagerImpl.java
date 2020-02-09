package com.kasian.trackme.property;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kasian.trackme.ALogger;
import com.kasian.trackme.data.CoordinateServerProperty;

import org.apache.log4j.Logger;

public class CoordinateServerInfoManagerImpl implements CoordinateServerInfoManager {
    private Context context;
    private static final String PROPERTY_SERVER = "address";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_PASSWORD = "password";
    private static final String PROPERTY_USER_ID = "user_id";
    private static final Logger LOG = ALogger.getLogger(CoordinateServerInfoManagerImpl.class);

    public CoordinateServerInfoManagerImpl(Context context) {
        this.context = context;
    }

    @Override
    public CoordinateServerProperty getCoordinateServerProperty() {
        LOG.info("getCoordinateServerProperty");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        CoordinateServerProperty property = CoordinateServerProperty.builder()
                .address(settings.getString(PROPERTY_SERVER, null))
                .user(settings.getString(PROPERTY_USER, null))
                .password(settings.getString(PROPERTY_PASSWORD, null))
                .userId(settings.getString(PROPERTY_USER_ID, null))
                .build();

        if (property.isComplete()) {
            return property;
        } else {
            return null;
        }
    }

    @Override
    public void setCoordinateServerProperty(CoordinateServerProperty serverProperty) {
        LOG.info("setCoordinateServerProperty, serverProperty=" + serverProperty);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(PROPERTY_SERVER, serverProperty.getAddress());
        edit.putString(PROPERTY_USER, serverProperty.getUser());
        edit.putString(PROPERTY_PASSWORD, serverProperty.getPassword());
        edit.putString(PROPERTY_USER_ID, serverProperty.getUserId());
        edit.apply();
    }
}
