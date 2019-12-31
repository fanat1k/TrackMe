package com.kasian.trackme.data;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CoordinateServerProperty {
    private String address;
    private String user;
    private String password;

    public boolean isPropertyValid() {
        return address != null && user != null && password != null;
    }

    @Override
    public String toString() {
        return "CoordinateServerProperty{" +
                "address='" + address + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
