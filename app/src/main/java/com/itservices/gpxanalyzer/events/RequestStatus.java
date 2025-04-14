package com.itservices.gpxanalyzer.events;

/**
 * Represents the status of chart operations in the GPX Analyzer application.
 * <p>
 * This enum defines all possible states that can occur during chart initialization,
 * data loading, processing, and updating. It helps track the progress of chart-related
 * operations and identify error conditions.
 * <p>
 * The enum values are ordered approximately from error states to successful completion states,
 * allowing for easy comparison of status severity. This ordering enables simple flow control
 * in various parts of the application where chart operations are performed.
 * <p>
 * This enum is used throughout the application to:
 * <ul>
 *   <li>Monitor the progress of asynchronous chart operations</li>
 *   <li>Provide feedback to users about chart loading states</li>
 *   <li>Enable reactive programming patterns with RxJava observables</li>
 *   <li>Control UI element states based on chart processing status</li>
 * </ul>
 */
public enum RequestStatus {
    /**
     * Error indicating that chart data sets are null.
     * This typically occurs when attempting to create or update a chart with no data.
     */
    ERROR_DATA_SETS_NULL,
    
    /**
     * Error indicating that a line data set is null.
     * This can occur when attempting to access or modify a specific data set that doesn't exist.
     */
    ERROR_LINE_DATA_SET_NULL,
    
    /**
     * Error indicating that a new data set to be added is null.
     * This occurs when trying to add an empty or invalid data set to a chart.
     */
    ERROR_NEW_DATA_SET_NULL,
    
    /**
     * Error indicating an invalid number of data sets to display.
     * This can occur when there are too many or too few data sets for proper visualization.
     */
    ERROR_INVALID_DATA_SET_AMOUNT_TO_SHOW,
    
    /**
     * Error indicating that the weak reference to the chart is null.
     * This typically indicates that a chart reference has been garbage collected
     * or was never properly initialized.
     */
    CHART_WEAK_REFERENCE_IS_NULL,
    
    /**
     * Error indicating that the chart object is null.
     * This occurs when attempting to operate on a chart that doesn't exist or has been destroyed.
     */
    CHART_IS_NULL,
    
    /**
     * General error state for unspecified errors.
     * This is a catch-all for unexpected errors not covered by more specific error states.
     */
    ERROR,
    
    /**
     * Default initial state before any operation has started.
     * This is the base state before any chart processing begins.
     */
    DEFAULT,
    
    /**
     * State indicating that data loading has started.
     * This indicates that the application is retrieving GPX data from sources.
     */
    LOADING,
    NEW_DATA_LOADING,
    
    /**
     * State indicating that data has been successfully loaded.
     * This indicates that GPX data has been retrieved and is ready for processing.
     */
    DATA_LOADED,
    
    /**
     * State indicating that data processing has started.
     * This indicates that loaded data is being transformed into chart-ready format.
     */
    PROCESSING,
    
    /**
     * State indicating that data processing has completed.
     * This indicates that data has been successfully transformed and is ready for chart display.
     */
    PROCESSED,
    
    /**
     * State indicating that the chart has been initialized.
     * This indicates that a chart has been created with basic settings but may not have data yet.
     */
    CHART_INITIALIZED,
    
    /**
     * State indicating that chart updating is in progress.
     * This indicates that new data or settings are being applied to an existing chart.
     */
    CHART_UPDATING,
    
    /**
     * State indicating that chart has been successfully updated.
     * This indicates that a chart has been refreshed with new data or settings.
     */
    CHART_UPDATED,
    
    /**
     * Final state indicating that all operations have completed successfully.
     * This indicates that the entire chart operation pipeline has completed without errors.
     */
    DONE
}
