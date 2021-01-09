package com.zhouj.endless.geohash.queries;

import com.zhouj.endless.geohash.GeoHash;
import com.zhouj.endless.geohash.WGS84Point;

import java.util.List;


public interface GeoHashQuery {

    /**
     * check wether a geohash is within the hashes that make up this query.
     */
    public boolean contains(GeoHash hash);

    /**
     * returns whether a point lies within a query.
     */
    public boolean contains(WGS84Point point);

    /**
     * should return the hashes that re required to perform this search.
     */
    public List<GeoHash> getSearchHashes();

    public String getWktBox();

}