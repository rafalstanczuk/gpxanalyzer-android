package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

public class EntryCacheMap {

    private ConcurrentMap<Long, Entry> entryMap = new ConcurrentHashMap<>();

    @Inject
    public EntryCacheMap() {
    }

    public void add(long timestampMillis, Entry entry) {
        entryMap.put(timestampMillis, entry);
    }

    public Entry get(long timestampMillis) {
        return entryMap.get(timestampMillis);
    }

    public void init(int n) {
        entryMap.clear();
        entryMap = new ConcurrentHashMap<>(n + 1);
    }
}
