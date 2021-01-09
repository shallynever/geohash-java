package com.zhouj.endless.geohash.util;

import com.zhouj.endless.geohash.GeoHash;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Iterate over all of the values within a bounding box at a particular
 * resolution
 */
public class BoundingBoxGeoHashIterator implements Iterator<GeoHash> {
    private TwoGeoHashBoundingBox boundingBox;
    private GeoHash current;

    public BoundingBoxGeoHashIterator(TwoGeoHashBoundingBox bbox) {
        boundingBox = bbox;
        current = bbox.getSouthWestCorner();
    }

    public TwoGeoHashBoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public boolean hasNext() {
        return current.compareTo(boundingBox.getNorthEastCorner()) <= 0;
    }

    @Override
    public GeoHash next() {
        GeoHash rv = current;
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        current = rv.next();
        while (hasNext() && !boundingBox.getBoundingBox().contains(current.getOriginatingPoint())) {
            current = current.next();
        }
        return rv;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
