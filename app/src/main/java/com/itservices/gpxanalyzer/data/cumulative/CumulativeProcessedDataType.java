package com.itservices.gpxanalyzer.data.cumulative;

/**
 * Defines types of cumulative processed data available for visualization.
 * <p>
 * This enum specifies different approaches for calculating cumulative values
 * when processing and visualizing GPX data. Each type represents a different
 * method of accumulating values across data entities, providing different
 * perspectives on the data for analysis and visualization.
 */
public enum CumulativeProcessedDataType {
    /**
     * Represents cumulative values calculated from the start of the current segment.
     * <p>
     * When this type is used, cumulative values are reset at the beginning of
     * each new trend segment and accumulate only within that segment. This allows
     * for analysis of changes within individual segments of similar characteristics.
     */
    FROM_SEGMENT_START_SUM_REAL_DELTA_CUMULATIVE_VALUE,
    
    /**
     * Represents cumulative values calculated across all processed segments.
     * <p>
     * When this type is used, cumulative values accumulate continuously across
     * the entire dataset, without resetting between segments. This allows for
     * analysis of overall changes across the entire track or route.
     */
    ALL_SUM_REAL_DELTA_CUMULATIVE_VALUE
}
