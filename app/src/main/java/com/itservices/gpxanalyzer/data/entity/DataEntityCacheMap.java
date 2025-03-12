package com.itservices.gpxanalyzer.data.entity;

import java.util.HashMap;
import java.util.Map;

final class DataEntityCacheMap {

    private Map<Long, DataEntity> dataEntityMap = new HashMap<>();


    public DataEntityCacheMap() {
    }

    public void add(long timestampMillis, DataEntity entry) {
        dataEntityMap.put(timestampMillis, entry);
    }

    public DataEntity get(long timestampMillis) {
        return dataEntityMap.get(timestampMillis);
    }

    public void init(int n) {
        dataEntityMap.clear();
        dataEntityMap = new HashMap<>(n + 1);
    }
}