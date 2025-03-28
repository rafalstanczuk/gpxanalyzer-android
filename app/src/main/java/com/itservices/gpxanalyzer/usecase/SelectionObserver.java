package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import com.itservices.gpxanalyzer.chart.ChartController;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.util.List;
import java.util.stream.IntStream;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Manages synchronization of selection events between multiple charts.
 * This class observes selection events from chart controllers and propagates
 * them to other charts, ensuring that all charts display selections in sync.
 * It enables coordinated viewing of data points across multiple chart visualizations.
 */
public class SelectionObserver {
    private static final String TAG = "SelectionObserver";
    private CompositeDisposable selectionCompositeDisposable = new CompositeDisposable();

    /**
     * Creates a new SelectionObserver instance.
     * Uses Dagger for dependency injection.
     */
    @Inject
    public SelectionObserver() {
    }

    /**
     * Initializes synchronization between a list of charts.
     * This method sets up bidirectional selection event propagation between
     * all valid pairs of charts in the provided list. When a selection
     * is made in any chart, it will be reflected in all other charts.
     *
     * @param charts The list of charts to synchronize
     */
    public void initChartSync(List<ChartAreaItem> charts) {
        if (charts == null || charts.isEmpty()) {
            Log.w(TAG, "Cannot initialize chart sync - chart list is null or empty");
            return;
        }

        Log.d(TAG, "Initializing chart sync for " + charts.size() + " charts");
        
        // Clean up existing subscriptions
        dispose();

        // Create a list of valid chart pairs for synchronization
        IntStream.range(0, charts.size())
            .filter(i -> isValidChart(charts.get(i), i))
            .forEach(i -> IntStream.range(0, charts.size())
                .filter(j -> i != j && isValidChart(charts.get(j), j))
                .forEach(j -> setupSync(charts.get(i), charts.get(j)))
            );

        Log.d(TAG, "Chart sync initialized successfully");
    }

    /**
     * Validates that a chart item is suitable for synchronization.
     * Checks that the chart and its controller are properly initialized.
     *
     * @param chart The chart to validate
     * @param index The index of the chart in the list (for logging)
     * @return true if the chart is valid for synchronization, false otherwise
     */
    private boolean isValidChart(ChartAreaItem chart, int index) {
        if (chart == null) {
            Log.w(TAG, "Chart is null at index " + index);
            return false;
        }
        
        ChartController controller = chart.getChartController();
        if (controller == null) {
            Log.w(TAG, "Chart controller is null at index " + index);
            return false;
        }

        if (controller.getChartAddress() == null) {
            Log.w(TAG, "Chart instance(Address) is null at index " + index);
            return false;
        }

        return true;
    }

    /**
     * Sets up synchronization between two charts.
     * This method subscribes to selection events from the source chart
     * and propagates them to the target chart.
     *
     * @param sourceChart The chart to observe selection events from
     * @param targetChart The chart to propagate selection events to
     */
    private void setupSync(ChartAreaItem sourceChart, ChartAreaItem targetChart) {
        ChartController sourceController = sourceChart.getChartController();
        ChartController targetController = targetChart.getChartController();

        Disposable disposable = sourceController.getSelection()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(baseEntry -> {
                if (baseEntry == null) {
                    Log.w(TAG, "Received null baseEntry in selection");
                    return;
                }

                if (baseEntry.getDataEntity() == null) {
                    Log.w(TAG, "Received null DataEntity in baseEntry");
                    return;
                }

                long timestamp = baseEntry.getDataEntity().timestampMillis();
                String sourceAddress = sourceController.getChartAddress();
                String targetAddress = targetController.getChartAddress();
                
                //Log.d(TAG, String.format("Syncing selection from chart %s to chart %s for timestamp: %d",
                //    sourceAddress, targetAddress, timestamp));
                
                try {
                    targetController.select(timestamp);
                } catch (Exception e) {
                    Log.e(TAG, String.format("Error selecting timestamp %d on chart %s", 
                        timestamp, targetAddress), e);
                }
            })
            .doOnError(throwable -> Log.e(TAG, "Error in chart sync", throwable))
            .subscribe();

        selectionCompositeDisposable.add(disposable);
    }

    /**
     * Disposes all selection synchronization subscriptions.
     * This method should be called when selection synchronization is no longer needed
     * or when preparing to set up new synchronization.
     */
    public void dispose() {
        Log.d(TAG, "Disposing selection subscriptions");
        ConcurrentUtil.tryToDispose(selectionCompositeDisposable);
        selectionCompositeDisposable = new CompositeDisposable();
    }

    /**
     * Handles notification that a new file has been loaded.
     * This method disposes existing selection subscriptions to prepare
     * for new synchronization setup.
     */
    public void onFileLoaded() {
        Log.d(TAG, "New file loaded - reinitializing chart sync");
        dispose(); // Clean up existing subscriptions
    }
}
