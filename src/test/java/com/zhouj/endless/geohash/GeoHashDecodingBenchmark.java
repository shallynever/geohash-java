package com.zhouj.endless.geohash;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/**
 * @author zhouj
 * @date 2020-12-23 10:24
 * @desc
 */
public class GeoHashDecodingBenchmark {
    private static final int NUMBER_OF_HASHES = 1000000;
    private static final char[] base32 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private String[] randomHashes;

    @Before
    public void setup() {
        randomHashes = new String[NUMBER_OF_HASHES];
        Random rand = new Random();
        for (int i = 0; i < NUMBER_OF_HASHES; i++) {
            // at least two chars
            int characters = rand.nextInt(10) + 2;
            StringBuilder string = new StringBuilder();
            for (int j = 0; j < characters; j++) {
                string.append(base32[rand.nextInt(base32.length)]);
            }
            randomHashes[i] = string.toString();
        }
    }

    @Test
    public void benchmarkRandomDecoding() {
        long start = System.currentTimeMillis();
        for (String hash : randomHashes) {
            GeoHash geoHash = GeoHash.fromGeohashString(hash);
            geoHash.toBase32();
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
