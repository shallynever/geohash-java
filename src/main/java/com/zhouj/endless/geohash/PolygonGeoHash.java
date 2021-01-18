package com.zhouj.endless.geohash;

import com.zhouj.endless.geohash.util.BoundingBoxGeoHashIterator;
import com.zhouj.endless.geohash.util.TwoGeoHashBoundingBox;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouj
 * @date 2020-12-28 14:16
 * @desc 多边形GeoHash
 */
public class PolygonGeoHash {

    private static Integer MAX_DESIRED_PRECISION = 12;
    private static Integer DEFAULT_MAX_DESIRED_PRECISION = 7;
    private static Integer DEFAULT_MIN_DESIRED_PRECISION = 4;
    private static Integer MIN_DESIRED_PRECISION = 1;
    private static Integer DEFAULT_MAX_GEO_HASH_COUNT = 8000;

    public static Map<Integer, List<Double>> geoHashErrMap = new HashMap();

    static {
        geoHashErrMap.put(4, Arrays.asList(0.17578125, 0.3515625));
        geoHashErrMap.put(5, Arrays.asList(0.0439453125, 0.0439453125));
        geoHashErrMap.put(6, Arrays.asList(0.0054931640625, 0.010986328125));
        geoHashErrMap.put(7, Arrays.asList(0.001373291015625, 0.001373291015625));
    }

    /**
     * 均匀边界分形
     *
     * @param sourcePointList     经纬度列表
     * @param minDesiredPrecision 最小期望精度
     * @param minDesiredPrecision 最大期望精度
     * @param maxGeoHashCount     geoHash最大数量
     * @return GeoHash字符串集合
     */
    public static Set<String> boundaryFractalEvenlySegmentation(List<WGS84Point> sourcePointList, Integer minDesiredPrecision, Integer maxDesiredPrecision, Integer maxGeoHashCount) {
        // 结果集合
        Set<String> geoHashResultSet = new HashSet<>();
        Set<GeoHash> geoHashSet = boundaryFractalEvenly(sourcePointList, minDesiredPrecision, maxDesiredPrecision, maxGeoHashCount);
        for (GeoHash geoHash : geoHashSet) {
            geoHashResultSet.add(geoHash.toBase32());
        }
        return geoHashResultSet;
    }

    /**
     * 边界分形，递归，不限制geoHash个数
     *
     * @param pointList 经纬度列表
     * @return GeoHash字符串集合
     */
    public static Set<String> boundaryFractalSegmentation(List<WGS84Point> pointList) {
        // 结果集合
        Set<String> geoHashResultSet = new HashSet<>();
        Set<GeoHash> geoHashSet = boundaryFractal(pointList);
        for (GeoHash geoHash : geoHashSet) {
            geoHashResultSet.add(geoHash.toBase32());
        }
        return geoHashResultSet;
    }

    /**
     * 瓦片分割
     *
     * @param pointList        地理围栏经纬度列表
     * @param desiredPrecision 期望精度
     * @return GeoHash字符串集合
     */
    public static Set<String> tilesSegmentation(List<WGS84Point> pointList, Integer desiredPrecision) {
        Set<String> geoHashResultSet = new HashSet<>();
        Set<GeoHash> geoHashSet = tiles(pointList, desiredPrecision);
        for (GeoHash geoHash : geoHashSet) {
            geoHashResultSet.add(geoHash.toBase32());
        }
        return geoHashResultSet;
    }

    /**
     * 按照位偏移
     *
     * @param pointList        地理围栏经纬度列表
     * @param desiredPrecision 期望精度
     * @return GeoHash字符串集合
     */
    public static Set<String> offsetStepSegmentation(List<WGS84Point> pointList, Integer desiredPrecision) {
        Set<String> geoHashResultSet = new HashSet<>();
        Set<GeoHash> geoHashSet = offsetStep(pointList, desiredPrecision);
        for (GeoHash geoHash : geoHashSet) {
            geoHashResultSet.add(geoHash.toBase32());
        }
        return geoHashResultSet;
    }

    /**
     * 均匀边界分形
     *
     * @param sourcePointList     经纬度列表
     * @param minDesiredPrecision 最小期望精度
     * @param minDesiredPrecision 最大期望精度
     * @param maxGeoHashCount     geoHash最大数量
     * @return GeoHash对象集合
     */
    public static Set<GeoHash> boundaryFractalEvenly(List<WGS84Point> sourcePointList, Integer minDesiredPrecision, Integer maxDesiredPrecision, Integer maxGeoHashCount) {
        // 结果集合
        Set<GeoHash> geoHashResultSet = new HashSet<>();
        // 地理围栏参数校验
        if (Objects.isNull(sourcePointList) || sourcePointList.size() < 3) {
            throw new IllegalArgumentException("地理围栏至少三个经纬度点");
        }
        // 设置默认值
        if (Objects.isNull(minDesiredPrecision)) {
            minDesiredPrecision = DEFAULT_MIN_DESIRED_PRECISION;
        }
        if (Objects.isNull(maxDesiredPrecision)) {
            maxDesiredPrecision = DEFAULT_MAX_DESIRED_PRECISION;
        }
        // GeoHash最小/最大精度长度区间校验
        if (!(minDesiredPrecision >= MIN_DESIRED_PRECISION && minDesiredPrecision <= maxDesiredPrecision && maxDesiredPrecision <= MAX_DESIRED_PRECISION)) {
            throw new IllegalArgumentException("0=<minDesiredPrecision<=maxDesiredPrecision<=12 ");
        }
        if (Objects.nonNull(maxGeoHashCount) && maxGeoHashCount >= Integer.MAX_VALUE) {
            maxGeoHashCount = Integer.MAX_VALUE;
        }
        if (Objects.isNull(maxGeoHashCount)) {
            maxGeoHashCount = DEFAULT_MAX_GEO_HASH_COUNT;
        }
        // 粗粒度GeoHash
        Set<GeoHash> geoHashSetMin = null;
        for (int i = minDesiredPrecision; i <= maxDesiredPrecision; i++) {
            geoHashSetMin = tiles(sourcePointList, i);
            if (geoHashSetMin.size() > maxGeoHashCount) {
                throw new IllegalArgumentException("地理围栏面积过大");
            }
            if (geoHashSetMin.size() > 0) {
                minDesiredPrecision = i;
                break;
            }
        }
        Set<GeoHash> remainingGeoHashSet = iterator(geoHashSetMin, sourcePointList, geoHashResultSet, maxGeoHashCount);
        if (geoHashResultSet.size() >= maxGeoHashCount) {
            return geoHashResultSet;
        }
        for (int i = minDesiredPrecision + 1; i <= maxDesiredPrecision; i++) {
            remainingGeoHashSet = appendToResultSet(remainingGeoHashSet, i, sourcePointList, geoHashResultSet, maxGeoHashCount);
            if (Objects.isNull(remainingGeoHashSet)) {
                break;
            }
            if (geoHashResultSet.size() >= maxGeoHashCount) {
                return geoHashResultSet;
            }
        }
        return geoHashResultSet;
    }

    /**
     * 边界分形，递归，不限制geoHash个数
     *
     * @param pointList 经纬度列表
     * @return GeoHash集合
     */
    public static Set<GeoHash> boundaryFractal(List<WGS84Point> pointList) {
        Set<GeoHash> geoHashResultSet = new HashSet<>();
        appendToResultSet(pointList, geoHashResultSet, pointList, 5);
        return geoHashResultSet;
    }

    /**
     * 瓦片思想，首先计算西南角GeoHash的中心点金纬度和经纬度偏移量，然后计算从西南角中心点出发到东北角，经纬度分别需要移动的步数，最后按照金纬度步数遍历
     *
     * @param pointList        经纬度列表
     * @param desiredPrecision 期望精度
     * @return GeoHash集合
     */
    public static Set<GeoHash> tiles(List<WGS84Point> pointList, Integer desiredPrecision) {
        if (pointList.size() < 3) {
            throw new IllegalArgumentException("至少选择三个点");
        }
        desiredPrecision = desiredPrecision == null ? DEFAULT_MAX_DESIRED_PRECISION : desiredPrecision;
        // 多边形外界框
        TwoGeoHashBoundingBox twoGeoHashBoundingBox = getTwoGeoHashBoundingBox(pointList, desiredPrecision);
        // 西南角
        BoundingBox southWestCornerBox = twoGeoHashBoundingBox.getSouthWestCorner().getBoundingBox();
        // 东北角
        BoundingBox northEastCornerBox = twoGeoHashBoundingBox.getNorthEastCorner().getBoundingBox();
        // 获取西南角GeoHash的中心点金纬度和经纬度偏移量
        LonLat lonLat = getLonLat(southWestCornerBox);
        // 经纬度移动步数
        double latitudeStep = Math.round((northEastCornerBox.getNorthLatitude() - southWestCornerBox.getSouthLatitude()) / lonLat.getPerLatitude());
        double longitudeStep = Math.round((northEastCornerBox.getEastLongitude() - southWestCornerBox.getWestLongitude()) / lonLat.getPerLongitude());
        // GeoHash集合
        Set<GeoHash> geoHashSet = new HashSet<>();
        for (double lat = 0; lat < latitudeStep; lat++) {
            for (double lon = 0; lon < longitudeStep; lon++) {
                double neighborLat = lonLat.latitude + lat * lonLat.getPerLatitude();
                double neighborLon = lonLat.longitude + lon * lonLat.getPerLongitude();
                if (isPointInPolygon(new WGS84Point(neighborLat, neighborLon), pointList)) {
                    geoHashSet.add(GeoHash.withCharacterPrecision(neighborLat, neighborLon, desiredPrecision));
                }
            }
        }
        return geoHashSet;
    }

    /**
     * 按照位偏移
     *
     * @param pointList        经纬度列表
     * @param desiredPrecision 期望精度
     * @return GeoHash集合
     */
    public static Set<GeoHash> offsetStep(List<WGS84Point> pointList, Integer desiredPrecision) {
        desiredPrecision = desiredPrecision == null ? DEFAULT_MAX_DESIRED_PRECISION : desiredPrecision;
        // 多边形外界框
        TwoGeoHashBoundingBox twoGeoHashBoundingBox = getTwoGeoHashBoundingBox(pointList, desiredPrecision);
        BoundingBoxGeoHashIterator iterator = new BoundingBoxGeoHashIterator(twoGeoHashBoundingBox);
        // GeoHash集合
        Set<GeoHash> geoHashSet = new HashSet<>();
        // 从西南角到东北角
        while (iterator.hasNext()) {
            geoHashSet.add(iterator.next());
        }
        return geoHashSet;
    }

    /**
     * 预测GeoHash长度
     *
     * @param pointList      经纬度列表
     * @param maxGeoHashSize 最大GeoHash数量
     * @return GeoHash长度
     */
    public static Integer predictionGeoHashLength(List<WGS84Point> pointList, Integer maxGeoHashSize) {
        if (pointList.size() < 3) {
            throw new RuntimeException("至少选择三个点");
        }
        maxGeoHashSize = maxGeoHashSize > DEFAULT_MAX_GEO_HASH_COUNT ? DEFAULT_MAX_GEO_HASH_COUNT : maxGeoHashSize;
        Integer predictionGeoHashLength = DEFAULT_MAX_DESIRED_PRECISION;
        double geoHashSize = 0;
        List<Integer> geoHashLengthList = geoHashErrMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (Integer geoHashLength : geoHashLengthList) {
            List<Double> latLon = geoHashErrMap.get(geoHashLength);
            // 多边形外界框
            TwoGeoHashBoundingBox twoGeoHashBoundingBox = getTwoGeoHashBoundingBox(pointList, geoHashLength);
            // 西南角
            BoundingBox southWestCornerBox = twoGeoHashBoundingBox.getSouthWestCorner().getBoundingBox();
            // 东北角
            BoundingBox northEastCornerBox = twoGeoHashBoundingBox.getNorthEastCorner().getBoundingBox();
            // 经纬度移动步数
            double latitudeStep = Math.round((northEastCornerBox.getNorthLatitude() - southWestCornerBox.getSouthLatitude()) / latLon.get(0));
            double longitudeStep = Math.round((northEastCornerBox.getEastLongitude() - southWestCornerBox.getWestLongitude()) / latLon.get(1));
            // GeoHash集合大小
            geoHashSize = latitudeStep * longitudeStep;
            if (geoHashSize <= maxGeoHashSize) {
                predictionGeoHashLength = geoHashLength;
                break;
            }
        }
        if (geoHashSize > maxGeoHashSize) {
            throw new IllegalArgumentException("所选围栏不合理");
        }
        return predictionGeoHashLength;
    }

    /**
     * 将地理围栏转为GeoHash添加到结果集合中,递归实现
     *
     * @param wgs84PointList   地理围栏集合
     * @param sourcePointList  源地理围栏经纬度列表
     * @param geoHashResultSet 结果
     */
    private static void appendToResultSet(List<WGS84Point> wgs84PointList, Set<GeoHash> geoHashResultSet, List<WGS84Point> sourcePointList, int desiredPrecision) {
        Set<GeoHash> geoHashSet = tiles(wgs84PointList, desiredPrecision);
        Iterator<GeoHash> iterator = geoHashSet.iterator();
        while (iterator.hasNext()) {
            GeoHash geoHash = iterator.next();
            // 判断GeoHash的四个点是否在在地理围栏内
            if (isGeoHashInPolygon(geoHash, sourcePointList)) {
                if (geoHashResultSet.size() > Long.MAX_VALUE) {
                    return;
                }
                geoHashResultSet.add(geoHash);
                iterator.remove();
            } else {
                // 如果精度大于7，中点在地理围栏内即可
                if (desiredPrecision > DEFAULT_MAX_DESIRED_PRECISION) {
                    if (isPointInPolygon(geoHash.getBoundingBox().getCenter(), sourcePointList)) {
                        geoHashResultSet.add(geoHash);
                    }
                } else {
                    BoundingBox box = geoHash.getBoundingBox();
                    List<WGS84Point> subWGS84PointList = Arrays.asList(box.getNorthWestCorner(), box.getNorthEastCorner(), box.getSouthEastCorner(), box.getSouthWestCorner());
                    appendToResultSet(subWGS84PointList, geoHashResultSet, sourcePointList, ++desiredPrecision);
                }
            }
            if (geoHashResultSet.size() > Long.MAX_VALUE) {
                return;
            }
        }
    }

    /**
     * 将粗粒度的GeoHash集合精细化，并添加到结果集中
     *
     * @param geoHashSet       需要精细化的GeoHash集合
     * @param desiredPrecision 期望GeoHash精度
     * @param sourcePointList  源地理围栏经纬度列表
     * @param geoHashResultSet 结果集合
     * @return 剩余需要精度细化的GeoHash集合
     */
    private static Set<GeoHash> appendToResultSet(Set<GeoHash> geoHashSet, Integer desiredPrecision, List<WGS84Point> sourcePointList, Set<GeoHash> geoHashResultSet, Integer maxGeoHashCount) {
        // GeoHash5
        Set<GeoHash> remainingGeoHashSet = new HashSet<>();
        // 精度细化
        for (GeoHash geoHash : geoHashSet) {
            BoundingBox box = geoHash.getBoundingBox();
            List<WGS84Point> wgs84PointList = Arrays.asList(box.getNorthWestCorner(), box.getNorthEastCorner(), box.getSouthEastCorner(), box.getSouthWestCorner());
            // GeoHash5
            Set<GeoHash> geoHashSet5 = tiles(wgs84PointList, desiredPrecision);
            remainingGeoHashSet.addAll(iterator(geoHashSet5, sourcePointList, geoHashResultSet, maxGeoHashCount));
        }
        if (geoHashResultSet.size() >= maxGeoHashCount) {
            return null;
        }
        return remainingGeoHashSet;
    }

    /**
     * 判断一个GeoHash区域是否大部分在多边形内，三个以上
     *
     * @param target 目标GeoHash
     * @param points 多边形
     * @return boolean
     */
    public static boolean isGeoHashSeemInPolygon(GeoHash target, List<WGS84Point> points) {
        BoundingBox geoHashBoundingBox = target.getBoundingBox();
        int count = 0;
        if (isPointInPolygon(geoHashBoundingBox.getNorthEastCorner(), points)) {
            count++;
        }
        if (isPointInPolygon(geoHashBoundingBox.getNorthWestCorner(), points)) {
            count++;
        }
        if (isPointInPolygon(geoHashBoundingBox.getSouthEastCorner(), points)) {
            count++;
        }
        if (isPointInPolygon(geoHashBoundingBox.getSouthWestCorner(), points)) {
            count++;
        }
        return count >= 3;
    }

    /**
     * 判断一个GeoHash区域是否在多边形内
     *
     * @param target 目标GeoHash
     * @param points 多边形
     * @return boolean
     */
    public static boolean isGeoHashInPolygon(GeoHash target, List<WGS84Point> points) {
        BoundingBox geoHashBoundingBox = target.getBoundingBox();
        boolean result = true;
        result &= isPointInPolygon(geoHashBoundingBox.getNorthEastCorner(), points);
        result &= isPointInPolygon(geoHashBoundingBox.getNorthWestCorner(), points);
        result &= isPointInPolygon(geoHashBoundingBox.getSouthEastCorner(), points);
        result &= isPointInPolygon(geoHashBoundingBox.getSouthWestCorner(), points);
        return result;
    }

    /**
     * 判断一点是否在多边形内
     *
     * @param target 目标点
     * @param points 多边形
     * @return boolean
     */
    public static boolean isPointInPolygon(WGS84Point target, List<WGS84Point> points) {
        int iSum, iCount, iIndex;
        double dLon1 = 0, dLon2 = 0, dLat1 = 0, dLat2 = 0, dLon;
        if (points.size() < 3) {
            return false;
        }
        iSum = 0;
        iCount = points.size();
        for (iIndex = 0; iIndex < iCount; iIndex++) {
            if (iIndex == iCount - 1) {
                dLon1 = points.get(iIndex).getLongitude();
                dLat1 = points.get(iIndex).getLatitude();
                dLon2 = points.get(0).getLongitude();
                dLat2 = points.get(0).getLatitude();
            } else {
                dLon1 = points.get(iIndex).getLongitude();
                dLat1 = points.get(iIndex).getLatitude();
                dLon2 = points.get(iIndex + 1).getLongitude();
                dLat2 = points.get(iIndex + 1).getLatitude();
            }
            double ALat = target.getLatitude();
            double ALon = target.getLongitude();
            // 以下语句判断A点是否在边的两端点的水平平行线之间，在则可能有交点，开始判断交点是否在左射线上
            if (((ALat >= dLat1) && (ALat < dLat2)) || ((ALat >= dLat2) && (ALat < dLat1))) {
                if (Math.abs(dLat1 - dLat2) > 0) {
                    //得到 A点向左射线与边的交点的x坐标：
                    dLon = dLon1 - ((dLon1 - dLon2) * (dLat1 - ALat)) / (dLat1 - dLat2);
                    // 如果交点在A点左侧（说明是做射线与边的交点），则射线与边的全部交点数加一：
                    if (dLon >= ALon) {
                        iSum++;
                    }
                }
            }
        }
        return (iSum % 2) != 0;
    }

    /**
     * 西南角&东北角
     *
     * @param pointList
     * @return
     */
    private static TwoGeoHashBoundingBox getTwoGeoHashBoundingBox(List<WGS84Point> pointList, Integer geoHashLength) {
        // 纬度集合
        List<Double> latitudeList = pointList.stream().map(WGS84Point::getLatitude).sorted().collect(Collectors.toList());
        double latitudeMin = latitudeList.get(0);
        double latitudeMax = latitudeList.get(latitudeList.size() - 1);
        // 经度集合
        List<Double> longitudeList = pointList.stream().map(WGS84Point::getLongitude).sorted().collect(Collectors.toList());
        double longitudeMin = longitudeList.get(0);
        double longitudeMax = longitudeList.get(longitudeList.size() - 1);
        // 多边形外界框
        BoundingBox rectangleBoundingBox = new BoundingBox(latitudeMin, latitudeMax, longitudeMin, longitudeMax);
        return TwoGeoHashBoundingBox.withCharacterPrecision(rectangleBoundingBox, geoHashLength);
    }

    /**
     * 获取金纬度
     *
     * @param boundingBox boundingBox
     * @return LonLat
     */
    private static LonLat getLonLat(BoundingBox boundingBox) {
        LonLat lonLat = new LonLat();
        // 中心点
        WGS84Point boxSouthWestCenter = boundingBox.getCenter();
        lonLat.setLatitude(boxSouthWestCenter.getLatitude());
        lonLat.setLongitude(boxSouthWestCenter.getLongitude());
        // 外界边框东北角
        WGS84Point boxSouthWestNorthEastCorner = boundingBox.getNorthEastCorner();
        // 每次移动的经纬度
        double perLatitude = (boxSouthWestNorthEastCorner.getLatitude() - boxSouthWestCenter.getLatitude()) * 2;
        double perLongitude = (boxSouthWestNorthEastCorner.getLongitude() - boxSouthWestCenter.getLongitude()) * 2;
        lonLat.setPerLatitude(perLatitude);
        lonLat.setPerLongitude(perLongitude);
        return lonLat;
    }

    private static Set<GeoHash> iterator(Set<GeoHash> geoHashSet, List<WGS84Point> sourcePointList, Set<GeoHash> geoHashResultSet, Integer maxGeoHashCount) {
        Set<GeoHash> remainingGeoHash = new HashSet<>();
        Iterator<GeoHash> iterator = geoHashSet.iterator();
        while (iterator.hasNext()) {
            GeoHash geoHash = iterator.next();
            if (geoHash.getCharacterPrecision() <= 5) {
                // 判断GeoHash的四个点是否在在地理围栏内
                if (isGeoHashInPolygon(geoHash, sourcePointList)) {
                    if (geoHashResultSet.size() > maxGeoHashCount) {
                        break;
                    }
                    geoHashResultSet.add(geoHash);
                } else {
                    remainingGeoHash.add(geoHash);
                }
            }
            if (geoHash.getCharacterPrecision() > 5) {
                // 判断GeoHash的三个点是否在在地理围栏内
                if (isGeoHashSeemInPolygon(geoHash, sourcePointList)) {
                    if (geoHashResultSet.size() > maxGeoHashCount) {
                        break;
                    }
                    geoHashResultSet.add(geoHash);
                } else {
                    remainingGeoHash.add(geoHash);
                }
            }
        }
        // 返回剩余
        return remainingGeoHash;
    }
}