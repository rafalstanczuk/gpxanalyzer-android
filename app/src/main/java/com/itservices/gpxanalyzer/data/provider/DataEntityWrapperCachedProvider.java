package com.itservices.gpxanalyzer.data.provider;

import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

public class DataEntityWrapperCachedProvider {

    private static final DataEntityWrapper DEFAULT_DATA_ENTITY_WRAPPER = new DataEntityWrapper(
            new Vector<>(
                    List.of(
                            new DataEntity(0,
                                    0L,
                                    List.of(0.0f),
                                    List.of(0.0f),
                                    List.of(""),
                                    List.of(""))
                    )
            ),
            0);
    private Map<Short, DataEntityWrapper> concurrentMap;

    @Inject
    public DataEntityWrapperCachedProvider() {
    }

    private static Map<Short, DataEntityWrapper> createCacheWith(DataEntity dataEntity) {
        return new ConcurrentHashMap<>(dataEntity.nameList().size());
    }

    public synchronized void clear() {
        concurrentMap.clear();
    }

    public synchronized DataEntityWrapper provide(final Vector<DataEntity> data, short primaryDataIndex) {

        if (concurrentMap == null) {
            if (data.isEmpty()) {
                return DEFAULT_DATA_ENTITY_WRAPPER;
            } else {
                concurrentMap = createCacheWith(data.firstElement());
            }
        }

        DataEntityWrapper dataEntityWrapperForIndex = concurrentMap.get(primaryDataIndex);

        if ( dataEntityWrapperForIndex == null
                ||
             isNotEqualByHash(data, dataEntityWrapperForIndex.getData())
        ) {
            dataEntityWrapperForIndex = new DataEntityWrapper(data, primaryDataIndex);
        }

        concurrentMap.put(primaryDataIndex, dataEntityWrapperForIndex);

        return dataEntityWrapperForIndex;
    }

    private static boolean isNotEqualByHash(Vector<DataEntity> data, Vector<DataEntity> dataSecond) {
        return Objects.hash(data) != Objects.hash(dataSecond);
    }
}
