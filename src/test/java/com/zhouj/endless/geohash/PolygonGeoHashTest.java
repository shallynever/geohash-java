package com.zhouj.endless.geohash;

import com.alibaba.fastjson.JSON;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

/**
 * @author zhouj
 * @date 2020-12-30 11:48
 * @desc
 */
public class PolygonGeoHashTest extends TestCase {

    @Test
    public void testTiles() throws InterruptedException {
        // 顺时针
        List<WGS84Point> pointListOne = Arrays.asList(
                new WGS84Point(0, 0),
                new WGS84Point(0.5, 0),
                new WGS84Point(0.5, 0.5),
                new WGS84Point(0, 0.5));
        System.out.println("pointListOne=> " + JSON.toJSONString(pointListOne));
        for (int i = 8; i < 9; i++) {
            long start = System.currentTimeMillis();
            Set<String> tilesGeoHashSet = PolygonGeoHash.tilesSegmentation(pointListOne, i);
            System.out.println("pointListOne==> " + i + " ======> " + tilesGeoHashSet.size() + " ======> " + (System.currentTimeMillis() - start));
        }

        System.out.println("===========================***************************===========================***************************");
        // 顺时针
        List<WGS84Point> pointListTwo = Arrays.asList(new WGS84Point(0, 0),
                new WGS84Point(0.1, 0),
                new WGS84Point(0.5, 0.2),
                new WGS84Point(0.5, 0.5),
                new WGS84Point(0.3, 0.5),
                new WGS84Point(0, 0.3));
        System.out.println("pointListTwo=> " + JSON.toJSONString(pointListTwo));
        for (int i = 8; i < 9; i++) {
            long start = System.currentTimeMillis();
            Set<String> tilesGeoHashList = PolygonGeoHash.tilesSegmentation(pointListTwo, i);
            System.out.println("pointListTwo==> " + i + " ======> " + tilesGeoHashList.size() + " ======> " + (System.currentTimeMillis() - start));
        }
    }

    @Test
    public void testBoundaryFractalComparedTiles() {
        // 顺时针
//        List<WGS84Point> pointList = Arrays.asList(new WGS84Point(0, 0),
//                new WGS84Point(10, 0),
//                new WGS84Point(10, 20),
//                new WGS84Point(0, 20));
        List<WGS84Point> pointList = Arrays.asList(new WGS84Point(0, 0),
                new WGS84Point(2, 0),
                new WGS84Point(2, 2),
                new WGS84Point(0, 2));
        System.out.println("<====>tiles<====>");
        for (int i = 4; i < 8; i++) {
            long start = System.currentTimeMillis();
            Set<GeoHash> tilesGeoHashList = PolygonGeoHash.tiles(pointList, i);
            System.out.println("tiles==> " + i + " ======> " + tilesGeoHashList.size() + " ======> " + (System.currentTimeMillis() - start));
        }

        System.out.println("<====>boundaryFractalEvenly<====>");
        long start = System.currentTimeMillis();
        Set<GeoHash> boundaryFractalSet = PolygonGeoHash.boundaryFractalEvenly(pointList, 4, 7, Integer.MAX_VALUE);
        System.out.println("boundaryFractal==> " + boundaryFractalSet.size() + " ======> " + (System.currentTimeMillis() - start));
        System.out.println("<============================================================>");
        Map<Integer, Long> statisticsMap = new HashMap<>();
        for (GeoHash geoHash : boundaryFractalSet) {
            if (Objects.isNull(statisticsMap.get(geoHash.getCharacterPrecision()))) {
                statisticsMap.put(geoHash.getCharacterPrecision(), 1L);
            } else {
                statisticsMap.put(geoHash.getCharacterPrecision(), statisticsMap.get(geoHash.getCharacterPrecision()) + 1);
            }
        }
        for (Map.Entry<Integer, Long> entry : statisticsMap.entrySet()) {
            System.out.println(entry.getKey() + " <==> " + entry.getValue());
        }
    }

    @Test
    public void testBoundaryFractalEvenlyByShuPan() {
        List<WGS84Point> pointList = Arrays.asList(new WGS84Point(30.339721, 120.068368),
                new WGS84Point(30.289839, 120.388021),
                new WGS84Point(30.078052, 120.293735),
                new WGS84Point(30.158026, 119.939587));
        System.out.println("<====>boundaryFractalEvenly<====>");
        long start = System.currentTimeMillis();
        Set<GeoHash> boundaryFractalSet = PolygonGeoHash.boundaryFractalEvenly(pointList, 4, 7, Integer.MAX_VALUE);
        System.out.println("boundaryFractal==> " + boundaryFractalSet.size() + " ======> " + (System.currentTimeMillis() - start));
        System.out.println("<============================================================>");
        Map<Integer, Long> statisticsMap = new HashMap<>();
        Set<String> gepHashSet = new HashSet<>(boundaryFractalSet.size());
        for (GeoHash geoHash : boundaryFractalSet) {
            if (Objects.isNull(statisticsMap.get(geoHash.getCharacterPrecision()))) {
                statisticsMap.put(geoHash.getCharacterPrecision(), 1L);
            } else {
                statisticsMap.put(geoHash.getCharacterPrecision(), statisticsMap.get(geoHash.getCharacterPrecision()) + 1);
            }
            gepHashSet.add(geoHash.toBase32());
        }
        for (Map.Entry<Integer, Long> entry : statisticsMap.entrySet()) {
            System.out.println(entry.getKey() + " <==> " + entry.getValue());
        }
        System.out.println(gepHashSet);
    }

    @Test
    public void testPredictionGeoHashLength() {
        List<WGS84Point> pointList = Arrays.asList(new WGS84Point(0, 0),
                new WGS84Point(0.1, 0),
                new WGS84Point(0.5, 0.2),
                new WGS84Point(0.5, 10),
                new WGS84Point(0.3, 0.5),
                new WGS84Point(10, 0.3));
        System.out.println(PolygonGeoHash.predictionGeoHashLength(pointList, 5000));
    }

    @Test
    public void testIsPointInPolygon() {
        WGS84Point target = new WGS84Point(0.439453125, 0.17578125);
        List<WGS84Point> pointList = Arrays.asList(
                new WGS84Point(0, 0),
                new WGS84Point(0.5, 0),
                new WGS84Point(0.5, 0.5),
                new WGS84Point(0, 0.5));
        System.out.println(PolygonGeoHash.isPointInPolygon(target, pointList));
    }
}