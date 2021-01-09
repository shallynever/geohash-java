package com.zhouj.endless.geohash;

/**
 * @author zhouj
 * @date 2020-12-29 15:13
 * @desc
 */
public class LonLat {
    double latitude;
    double longitude;
    double perLatitude;
    double perLongitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getPerLatitude() {
        return perLatitude;
    }

    public void setPerLatitude(double perLatitude) {
        this.perLatitude = perLatitude;
    }

    public double getPerLongitude() {
        return perLongitude;
    }

    public void setPerLongitude(double perLongitude) {
        this.perLongitude = perLongitude;
    }
}
