package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.subjects.PublishSubject;

/**
 * Implements a RecyclerView adapter data observer for ChartAreaItems.
 * This class observes changes in a RecyclerView adapter and publishes notifications
 * about changed items to subscribers. It provides a reactive way to respond
 * to changes in the chart item collection displayed by a RecyclerView.
 * 
 * The observer maintains a reference to the chart items list and publishes
 * the relevant subset of items when changes occur in the adapter.
 */
public class AdapterDataObserverImpl extends RecyclerView.AdapterDataObserver {
    private final PublishSubject<List<ChartAreaItem>> changedItems = PublishSubject.create();
    private final List<ChartAreaItem> chartAreaItemList;

    /**
     * Creates a new AdapterDataObserverImpl.
     * 
     * @param chartAreaItemList The list of chart items to observe changes for
     */
    @Inject
    public AdapterDataObserverImpl(List<ChartAreaItem> chartAreaItemList) {
        this.chartAreaItemList = chartAreaItemList;
    }

    /**
     * Gets the PublishSubject that emits lists of changed chart items.
     * 
     * @return A PublishSubject that emits lists of changed ChartAreaItems
     */
    public PublishSubject<List<ChartAreaItem>> getChangedItems() {
        return changedItems;
    }

    /**
     * Called when the entire data set has changed.
     * Publishes the entire chart item list as changed.
     */
    @Override
    public void onChanged() {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onChanged() called");

        changedItems.onNext(
                chartAreaItemList
        );
    }

    /**
     * Called when a range of items has changed.
     * Publishes only the affected range of chart items.
     * 
     * @param positionStart The starting position of the changed range
     * @param itemCount The number of items that have changed
     */
    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeChanged() called with: positionStart = [" + positionStart + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList.subList(positionStart, positionStart + itemCount)
        );
    }

    /**
     * Called when items have been inserted into the adapter.
     * Publishes the entire chart item list to reflect the insertion.
     * 
     * @param positionStart The starting position for the insertion
     * @param itemCount The number of items inserted
     */
    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeInserted() called with: positionStart = [" + positionStart + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList
        );
    }

    /**
     * Called when items have been removed from the adapter.
     * Publishes the entire chart item list to reflect the removal.
     * 
     * @param positionStart The starting position for the removal
     * @param itemCount The number of items removed
     */
    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeRemoved() called with: positionStart = [" + positionStart + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList
        );
    }

    /**
     * Called when items have been moved within the adapter.
     * Publishes the entire chart item list to reflect the movement.
     * 
     * @param fromPosition The starting position of the item being moved
     * @param toPosition The destination position of the moved item
     * @param itemCount The number of items moved
     */
    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeMoved() called with: fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList
        );
    }
}
