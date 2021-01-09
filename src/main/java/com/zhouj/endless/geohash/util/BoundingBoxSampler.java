package com.zhouj.endless.geohash.util;

import com.zhouj.endless.geohash.GeoHash;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Select random samples of geohashes within a bounding box, without replacement
 */
public class BoundingBoxSampler {
    private TwoGeoHashBoundingBox boundingBox;
    private Set<Integer> alreadyUsed = new HashSet<>();
    private int maxSamples;
    private Random rand = new Random();

    /**
     * @param bbox
     * @throws IllegalArgumentException if the number of geohashes contained in the bounding box
     *                                  exceeds Integer.MAX_VALUE
     */
    public BoundingBoxSampler(TwoGeoHashBoundingBox bbox) {
        boundingBox = bbox;
        long maxSamplesLong = GeoHash.stepsBetween(bbox.getSouthWestCorner(), bbox.getNorthEastCorner());
        if (maxSamplesLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("This bounding box is too big too sample using this algorithm");
        }
        maxSamples = (int) maxSamplesLong;
    }

    public BoundingBoxSampler(TwoGeoHashBoundingBox bbox, long seed) {
        this(bbox);
        rand = new Random(seed);
    }

    public TwoGeoHashBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * @return next sample, or NULL if all samples have been returned
     */
    public GeoHash next() {
        if (alreadyUsed.size() == maxSamples) {
            return null;
        }
        int idx = rand.nextInt(maxSamples + 1);
        while (alreadyUsed.contains(idx)) {
            idx = rand.nextInt(maxSamples + 1);
        }
        alreadyUsed.add(idx);
        GeoHash gh = boundingBox.getSouthWestCorner().next(idx);
        if (!boundingBox.getBoundingBox().contains(gh.getOriginatingPoint())) {
            return next();
        }
        return gh;
    }
}
