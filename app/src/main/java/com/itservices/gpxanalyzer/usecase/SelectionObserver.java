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

public class SelectionObserver {
    private static final String TAG = "SelectionObserver";
    private CompositeDisposable selectionCompositeDisposable = new CompositeDisposable();

    @Inject
    public SelectionObserver() {
    }

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

    public void dispose() {
        Log.d(TAG, "Disposing selection subscriptions");
        ConcurrentUtil.tryToDispose(selectionCompositeDisposable);
        selectionCompositeDisposable = new CompositeDisposable();
    }

    public void onFileLoaded() {
        Log.d(TAG, "New file loaded - reinitializing chart sync");
        dispose(); // Clean up existing subscriptions
    }
}
