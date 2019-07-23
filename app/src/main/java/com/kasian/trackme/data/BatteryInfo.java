package com.kasian.trackme.data;

import lombok.Data;

@Data
public class BatteryInfo {
    private Integer batteryLevel;
    private boolean isCharging;
}
