package com.zhouj.endless.geohash.util;

import com.zhouj.endless.geohash.BoundingBox;
import com.zhouj.endless.geohash.GeoHash;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Created by IntelliJ IDEA. User: kevin Date: Jan 17, 2011 Time: 12:48:55 PM
 */
public class BoundingBoxSamplerTest {
    @Test
    public void testSampler() {
        BoundingBox bbox = new BoundingBox(37.7, 37.84, -122.52, -122.35);
        BoundingBoxSampler sampler = new BoundingBoxSampler(TwoGeoHashBoundingBox.withBitPrecision(bbox, 35), 1179);
        bbox = sampler.getBoundingBox().getBoundingBox();
        GeoHash gh = sampler.next();
        Set<String> hashes = new HashSet<>();
        int sumOfComp = 0;
        int crossingZero = 0;

        GeoHash prev = null;
        while (gh != null) {
            assertTrue(bbox.contains(gh.getOriginatingPoint()));
            assertFalse(hashes.contains(gh.toBase32()));
            hashes.add(gh.toBase32());
            if (prev != null) {
                sumOfComp += prev.compareTo(gh);
            }
            prev = gh;
            if (sumOfComp == 0) {
                crossingZero++;
            }
            gh = sampler.next();
        }
        assertEquals(12875, hashes.size());
        // The expected value of the sum should be zero. This checks that it is
        // at least close. Worst case is 12875 or -12875 so -40 is sufficiently
        // close
        assertEquals(-40, sumOfComp);
        // Check that the sum is zero a number of times, to make sure values are
        // increasing and decreasing.
        assertEquals(123, crossingZero);
    }
}
