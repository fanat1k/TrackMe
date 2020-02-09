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
    @Nullable
    private String userId;  // user identity, for now it's user's login

    private static final CoordinateServerInfoHolder instance = new CoordinateServerInfoHolder();

    private CoordinateServerInfoHolder() {
    }

    public static CoordinateServerInfoHolder getInstance() {
        return instance;
    }

    public void setProperty(CoordinateServerProperty property) {
        instance.setServer(property.getAddress());
        instance.setUser(property.getUser());
        instance.setPassword(property.getPassword());
        instance.setUserId(property.getUserId());
    }


    public boolean isComplete() {
        return server != null && user != null && password != null && userId != null;
    }
}
