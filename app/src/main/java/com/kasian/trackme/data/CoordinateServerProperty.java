package com.kasian.trackme.data;

import com.kasian.trackme.Utils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CoordinateServerProperty {
    private String address;
    private String user;
    private String password;
    private String userId;

    public boolean isComplete() {
        return address != null && user != null && password != null && userId != null;
    }

    @Override
    public String toString() {
        return "CoordinateServerProperty{" +
                "address='" + address + '\'' +
                ", user='" + user + '\'' +
                ", password='" + Utils.hidePassword(password) + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
