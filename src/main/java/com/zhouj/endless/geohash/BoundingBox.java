package com.zhouj.endless.geohash;

import java.io.Serializable;

/**
 * 地理围栏
 */
public class BoundingBox implements Serializable {
    private static final long serialVersionUID = -7145192134410261076L;
    private double southLatitude;
    private double northLatitude;
    private double westLongitude;
    private double eastLongitude;
    private boolean intersects180Meridian;

    /**
     * create a bounding box defined by two coordinates
     */
    public BoundingBox(WGS84Point southWestCorner, WGS84Point northEastCorner) {
        this(southWestCorner.getLatitude(), northEastCorner.getLatitude(), southWestCorner.getLongitude(), northEastCorner.getLongitude());
    }

    /**
     * Create a bounding box with the specified latitudes and longitudes. This constructor takes the order of the points into account.
     *
     * @param northLatitude
     * @param southLatitude
     * @param westLongitude
     * @param eastLongitude
     * @throws IllegalArgumentException When the defined BoundingBox would go over one of the poles. This kind of box is not supported.
     */
    public BoundingBox(double southLatitude, double northLatitude, double westLongitude, double eastLongitude) {
        if (southLatitude > northLatitude) {
            throw new IllegalArgumentException("The southLatitude must not be greater than the northLatitude");
        }

        if (Math.abs(southLatitude) > 90 || Math.abs(northLatitude) > 90 || Math.abs(westLongitude) > 180 || Math.abs(eastLongitude) > 180) {
            throw new IllegalArgumentException("The supplied coordinates are out of range.");
        }

        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;

        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;

        intersects180Meridian = eastLongitude < westLongitude;
    }

    /**
     * Clone constructor
     *
     * @param that
     */
    public BoundingBox(BoundingBox that) {
        this(that.southLatitude, that.northLatitude, that.westLongitude, that.eastLongitude);
    }

    /**
     * Returns the NorthWestCorner of this BoundingBox as a new Point.
     *
     * @return 西北角
     */
    public WGS84Point getNorthWestCorner() {
        return new WGS84Point(northLatitude, westLongitude);
    }

    /**
     * Returns the NorthEastCorner of this BoundingBox as a new Point.
     *
     * @return 东北角
     */
    public WGS84Point getNorthEastCorner() {
        return new WGS84Point(northLatitude, eastLongitude);
    }

    /**
     * Returns the SouthEastCorner of this BoundingBox as a new Point.
     *
     * @return 东南角
     */
    public WGS84Point getSouthEastCorner() {
        return new WGS84Point(southLatitude, eastLongitude);
    }

    /**
     * Returns the SouthWestCorner of this BoundingBox as a new Point.
     *
     * @return 西南角
     */
    public WGS84Point getSouthWestCorner() {
        return new WGS84Point(southLatitude, westLongitude);
    }

    /**
     * Returns the size of the bounding box in degrees of latitude. The value returned will always be positive.
     *
     * @return 维度大小
     */
    public double getLatitudeSize() {
        return northLatitude - southLatitude;
    }

    /**
     * Returns the size of the bounding box in degrees of longitude. The value returned will always be positive.
     *
     * @return 经度大小
     */
    public double getLongitudeSize() {
        if (eastLongitude == 180.0 && westLongitude == -180.0) {
            return 360.0;
        }
        double size = (eastLongitude - westLongitude) % 360;

        // Remainder fix for earlier java versions
        if (size < 0) {
            size += 360.0;
        }
        return size;
    }

    /**
     * 判断一个点是否在地理围栏内
     *
     * @param point WGS84Point
     * @return boolean
     */
    public boolean contains(WGS84Point point) {
        return containsLatitude(point.getLatitude()) && containsLongitude(point.getLongitude());
    }

    /**
     * 判断两个地理围栏是否相交
     *
     * @param other BoundingBox
     * @return boolean
     */
    public boolean intersects(BoundingBox other) {
        // Check latitude first cause it's the same for all cases
        if (other.southLatitude > northLatitude || other.northLatitude < southLatitude) {
            return false;
        } else {
            if (!intersects180Meridian && !other.intersects180Meridian) {
                return !(other.eastLongitude < westLongitude || other.westLongitude > eastLongitude);
            } else if (intersects180Meridian && !other.intersects180Meridian) {
                return !(eastLongitude < other.westLongitude && westLongitude > other.eastLongitude);
            } else if (!intersects180Meridian && other.intersects180Meridian) {
                return !(westLongitude > other.eastLongitude && eastLongitude < other.westLongitude);
            } else {
                return true;
            }
        }
    }

    /**
     * 获取地理围栏中心点
     *
     * @return WGS84Point
     */
    public WGS84Point getCenter() {
        double centerLatitude = (southLatitude + northLatitude) / 2;
        double centerLongitude = (westLongitude + eastLongitude) / 2;

        // This can happen if the bbox crosses the 180-meridian
        if (centerLongitude > 180) {
            centerLongitude -= 360;
        }

        return new WGS84Point(centerLatitude, centerLongitude);
    }

    /**
     * Expands this bounding box to include the provided bounding box. The expansion is done in the direction with the minimal distance. If both distances are the same it'll expand
     * in east direction. It will not cross poles, but it will cross the 180-Meridian, if thats the shortest distance.<br>
     * If a precise specification of the northEast and southWest points is needed, please create a new bounding box where you can specify the points separately.
     *
     * @param other
     */
    public void expandToInclude(BoundingBox other) {

        // Expand Latitude
        if (other.southLatitude < southLatitude) {
            southLatitude = other.southLatitude;
        }
        if (other.northLatitude > northLatitude) {
            northLatitude = other.northLatitude;
        }

        // Expand Longitude
        // At first check whether the two boxes contain each other or not
        boolean thisContainsOther = containsLongitude(other.eastLongitude) && containsLongitude(other.westLongitude);
        boolean otherContainsThis = other.containsLongitude(eastLongitude) && other.containsLongitude(westLongitude);

        // The new box needs to span the whole globe
        if (thisContainsOther && otherContainsThis) {
            eastLongitude = 180.0;
            westLongitude = -180.0;
            intersects180Meridian = false;
            return;
        }
        // Already done in this case
        if (thisContainsOther) {
            return;
        }
        // Expand to match the bigger box
        if (otherContainsThis) {
            eastLongitude = other.eastLongitude;
            westLongitude = other.westLongitude;
            intersects180Meridian = eastLongitude < westLongitude;
            return;
        }

        // If this is not the case compute the distance between the endpoints in east direction
        double distanceEastToOtherEast = (other.eastLongitude - eastLongitude) % 360;
        double distanceOtherWestToWest = (westLongitude - other.westLongitude) % 360;

        // Fix for lower java versions, since the remainder-operator (%) changed in one version, idk which one
        if (distanceEastToOtherEast < 0) {
            distanceEastToOtherEast += 360;
        }
        if (distanceOtherWestToWest < 0) {
            distanceOtherWestToWest += 360;
        }

        // The minimal distance needs to be extended
        if (distanceEastToOtherEast <= distanceOtherWestToWest) {
            eastLongitude = other.eastLongitude;
        } else {
            westLongitude = other.westLongitude;
        }

        intersects180Meridian = eastLongitude < westLongitude;
    }

    public double getEastLongitude() {
        return eastLongitude;
    }

    public double getWestLongitude() {
        return westLongitude;
    }

    public double getNorthLatitude() {
        return northLatitude;
    }

    public double getSouthLatitude() {
        return southLatitude;
    }

    public boolean intersects180Meridian() {
        return intersects180Meridian;
    }

    private boolean containsLatitude(double latitude) {
        return latitude >= southLatitude && latitude <= northLatitude;
    }

    private boolean containsLongitude(double longitude) {
        if (intersects180Meridian) {
            return longitude <= eastLongitude || longitude >= westLongitude;
        } else {
            return longitude >= westLongitude && longitude <= eastLongitude;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BoundingBox) {
            BoundingBox that = (BoundingBox) obj;
            return southLatitude == that.southLatitude && westLongitude == that.westLongitude && northLatitude == that.northLatitude && eastLongitude == that.eastLongitude;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCode(southLatitude);
        result = 37 * result + hashCode(northLatitude);
        result = 37 * result + hashCode(westLongitude);
        result = 37 * result + hashCode(eastLongitude);
        return result;
    }

    private static int hashCode(double x) {
        long f = Double.doubleToLongBits(x);
        return (int) (f ^ (f >>> 32));
    }

    @Override
    public String toString() {
        return getNorthWestCorner() + " -> " + getSouthEastCorner();
    }
}
