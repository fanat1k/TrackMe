package com.kasian.trackme;

public class BatteryInfo {
    private Integer batteryLevel;
    private boolean isCharging;

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }

    @Override
    public String toString() {
        return "BatteryInfo{" +
                "batteryLevel=" + batteryLevel +
                ", isCharging=" + isCharging +
                '}';
    }
}
