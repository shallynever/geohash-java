package com.zhouj.endless.geohash;

import com.zhouj.endless.geohash.util.RandomGeohashes;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhouj
 * @date 2020-12-23 10:25
 * @desc
 */
public class GeoHashEncodingBenchmark {
    private static final int NUMBER_OF_HASHES = 1000000;
    private GeoHash[] hashes;

    @Before
    public void setupBenchmark() {
        hashes = new GeoHash[NUMBER_OF_HASHES];
    }

    @Test
    public void benchmarkGeoHashEncoding() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_HASHES; i++) {
            hashes[i] = RandomGeohashes.createWithPrecision(60);
            hashes[i].toBase32();
//            System.out.println(hashes[i].bits);
//            System.out.println(GeoHash.fromLongValue(hashes[i].bits,hashes[i].significantBits));
        }
        System.out.println(System.currentTimeMillis() - start);

    }
}
