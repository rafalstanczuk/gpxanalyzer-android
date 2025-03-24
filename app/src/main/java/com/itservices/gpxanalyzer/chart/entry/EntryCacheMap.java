package com.itservices.gpxanalyzer.chart.entry;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

public class EntryCacheMap {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAX_CACHE_SIZE = 50000; // Limit cache size to prevent OOM
    
    private ConcurrentMap<Long, Entry> entryMap = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);

    @Inject
    public EntryCacheMap() {
    }

    public void add(long timestampMillis, Entry entry) {
        // Don't add if we're at capacity to prevent memory issues
        if (entryMap.size() >= MAX_CACHE_SIZE) {
            Log.i(EntryCacheMap.class.getSimpleName(), "add() Don't add if we're at capacity to prevent memory issues = [" + timestampMillis + "], entry = [" + entry + "]");
            return;
        }
        entryMap.put(timestampMillis, entry);
    }

    public Entry get(long timestampMillis) {
        return entryMap.get(timestampMillis);
    }

    public void init(DataEntityWrapper dataEntityWrapper) {
        int n = dataEntityWrapper.getData().size();

        entryMap.clear();
        // Use a reasonable initial capacity based on expected size
        int capacity = Math.min(n + 1, MAX_CACHE_SIZE);
        entryMap = new ConcurrentHashMap<>(capacity);
    }

    public void clear() {
        entryMap.clear();
    }

    public void update(List<LineDataSet> lineDataSetList) {
        clear();
        lineDataSetList.forEach(lineDataSet -> {
            lineDataSet.getEntries().forEach(entry -> {
                add(((BaseEntry) entry).getDataEntity().timestampMillis(), entry);
            });
        });
    }
}
