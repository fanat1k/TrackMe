package com.kasian.trackme.data;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HealthCheck {
    private Status status;
    private String locationLastUpdateTime;
    private String locationLastSendTime;
    private int coordinateCacheSize;
    private CoordinateServerProperty coordinateServerInfo;



    public enum Status {
        ON(true),
        OFF(false);

        private boolean status;

        Status(boolean status) {
            this.status = status;
        }

        public static Status parse(final boolean status) {
            for (Status s : Status.values()) {
                if (s.status == status) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Bad status value: " + status);
        }
    }
}
