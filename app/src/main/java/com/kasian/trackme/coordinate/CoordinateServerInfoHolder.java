package com.kasian.trackme.coordinate;

import android.support.annotation.Nullable;

import com.kasian.trackme.data.CoordinateServerProperty;

import lombok.Data;

@Data
public class CoordinateServerInfoHolder {
    @Nullable
    private String server;
    @Nullable
    private String user;
    @Nullable
    private String password;

    private static final CoordinateServerInfoHolder instance = new CoordinateServerInfoHolder();

    private CoordinateServerInfoHolder() {
    }

    public static CoordinateServerInfoHolder getInstance() {
        return instance;
    }

    public boolean isReady() {
        return server != null && user != null && password != null;
    }

    public void setProperty(CoordinateServerProperty property) {
        instance.setServer(property.getAddress());
        instance.setUser(property.getUser());
        instance.setPassword(property.getPassword());
    }
}
