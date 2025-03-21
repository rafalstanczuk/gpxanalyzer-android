package com.itservices.gpxanalyzer.data.provider;

import android.util.Log;

import com.github.mikephil.charting.data.LineDataSet;
import com.itservices.gpxanalyzer.chart.LineChartSettings;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LineDataSetListCachedProvider {

    private final Map<DataEntityWrapper, AtomicReference<List<LineDataSet>>> concurrentMap = new ConcurrentHashMap<>();


    @Inject
    public LineDataSetListCachedProvider() {
    }

    public void clear() {
        concurrentMap.clear();
    }

    public List<LineDataSet> provide(DataEntityWrapper dataEntityWrapper, LineChartSettings settings) {

        clearOldCachedData(dataEntityWrapper);

        AtomicReference<List<LineDataSet>> listAtomicReference = concurrentMap.get(dataEntityWrapper);

        if (listAtomicReference == null) {
            return null;
        }

        settings.updateSettingsFor(listAtomicReference.get());

        return new ArrayList<>(listAtomicReference.get());
    }

    private void clearOldCachedData(DataEntityWrapper newData) {
        //Log.d(LineDataSetListCachedProvider.class.getSimpleName(), "clearOldCachedData() called with: concurrentMap.keySet() = [" + concurrentMap.keySet() + "]");

        concurrentMap.keySet().forEach(
                key -> {
                   if (isNotEqualByHash(key.getData(), newData.getData())) {

                       Log.d(LineDataSetListCachedProvider.class.getSimpleName(), "clearOldCachedData() remove: key = [" + key + "]");

                       concurrentMap.remove(key);
                   }
                }
        );

        Log.d(LineDataSetListCachedProvider.class.getSimpleName(), "clearOldCachedData() cleaned: concurrentMap.keySet() = [" + concurrentMap.keySet() + "]");

    }

    public void add(DataEntityWrapper dataEntityWrapper, List<LineDataSet> lineDataSetList) {
        concurrentMap.put(dataEntityWrapper, new AtomicReference<>(new ArrayList<>(lineDataSetList)));
    }

    private static boolean isNotEqualByHash(Vector<DataEntity> data, Vector<DataEntity> dataSecond) {
        return Objects.hash(data) != Objects.hash(dataSecond);
    }
}
