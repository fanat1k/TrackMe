package com.kasian.trackme.property;

import com.kasian.trackme.data.CoordinateServerProperty;

public interface CoordinateServerInfoManager {
    CoordinateServerProperty getCoordinateServerProperty();
    void setCoordinateServerProperty(CoordinateServerProperty serverProperty);
}
