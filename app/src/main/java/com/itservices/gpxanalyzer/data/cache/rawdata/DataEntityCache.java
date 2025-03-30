package com.itservices.gpxanalyzer.data.cache.rawdata;

import com.itservices.gpxanalyzer.data.raw.DataEntity;
import com.itservices.gpxanalyzer.data.statistics.DataEntityStatistics;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A singleton provider class that manages cached DataEntity objects and their statistics.
 * This class maintains a thread-safe collection of DataEntity objects and provides
 * methods for adding, retrieving, and analyzing the data.
 *
 * The class uses AtomicReference for thread-safe access to the data collection and
 * maintains running statistics for the data entities. It is designed to be used with
 * dependency injection and provides a centralized cache for data entities.
 */
@Singleton
public class DataEntityCache {

    private AtomicReference<Vector<DataEntity>> dataEntityVector = new AtomicReference<>(new Vector<>());
    private DataEntityStatistics dataEntityStatistics;

    /**
     * Creates a new DataEntityCachedProvider with initial statistics for one measure.
     */
    @Inject
    public DataEntityCache() {
        dataEntityStatistics = new DataEntityStatistics(1);
    }

    /**
     * Initializes the provider with the specified number of measures and clears any existing data.
     *
     * @param nPrimaryIndexes The number of different measures to track
     */
    public void init(int nPrimaryIndexes) {
        dataEntityStatistics = new DataEntityStatistics(nPrimaryIndexes);
        dataEntityVector.get().clear();
    }

    /**
     * Adds a single DataEntity to the cache and updates the statistics.
     *
     * @param dataEntity The DataEntity to add
     */
    public void accept(DataEntity dataEntity) {
        dataEntityVector.get().add(dataEntity);
        dataEntityStatistics.accept(dataEntity);
    }

    /**
     * Adds multiple DataEntity objects to the cache and updates the statistics.
     * This method clears any existing data before adding the new entities.
     *
     * @param dataEntityList The list of DataEntity objects to add
     */
    public void acceptAll(List<DataEntity> dataEntityList) {
        if(dataEntityList.isEmpty()) {
            return;
        }
        this.dataEntityVector.get().clear();
        this.dataEntityVector.get().addAll(dataEntityList);

        dataEntityStatistics = new DataEntityStatistics(dataEntityList.get(0).getMeasures().size());
        dataEntityStatistics.acceptAll(dataEntityList);
    }

    /**
     * Resets the cache and statistics to their initial state.
     */
    public void reset() {
        this.dataEntityVector.get().clear();
        dataEntityStatistics.reset();
    }

    /**
     * Checks if the cache is empty.
     *
     * @return true if no data entities are cached, false otherwise
     */
    public boolean isEmpty() {
        return dataEntityStatistics.isEmpty();
    }

    /**
     * Returns the statistics for the cached data entities.
     *
     * @return The DataEntityStatistics object
     */
    public DataEntityStatistics getDataEntityStatistics() {
        return dataEntityStatistics;
    }

    /**
     * Returns the vector of cached data entities.
     *
     * @return The vector of DataEntity objects
     */
    public Vector<DataEntity> getDataEntitityVector() {
        return dataEntityVector.get();
    }
}