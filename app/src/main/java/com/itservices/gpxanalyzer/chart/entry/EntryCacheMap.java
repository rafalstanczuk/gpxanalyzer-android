package com.itservices.gpxanalyzer.chart.entry;

import com.github.mikephil.charting.data.Entry;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class EntryCacheMap {

    private Map<Long, Entry> entryMap = new HashMap<>();

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
        entryMap = new HashMap<>(n + 1);
    }
}
