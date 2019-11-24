package com.kasian.trackme;

public class CoordinateServerInfo {
    private String coordinateServer;
    private String user;
    private String password;

    private static final CoordinateServerInfo instance = new CoordinateServerInfo();

    private CoordinateServerInfo() {
    }

    public static CoordinateServerInfo getInstance() {
        return instance;
    }

    public boolean isReady() {
        return coordinateServer != null && user != null && password != null;
    }

    public String getCoordinateServer() {
        return coordinateServer;
    }

    public void setCoordinateServer(String coordinateServer) {
        this.coordinateServer = coordinateServer;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
