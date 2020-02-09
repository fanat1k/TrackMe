package com.kasian.trackme.data;

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
        String hiddenPassword = this.password.replaceFirst(".{2}", "*");
        return "CoordinateServerProperty{" +
                "address='" + address + '\'' +
                ", user='" + user + '\'' +
                ", password='" + hiddenPassword + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
