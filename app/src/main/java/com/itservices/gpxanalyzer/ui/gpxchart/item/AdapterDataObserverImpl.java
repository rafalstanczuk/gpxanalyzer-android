package com.itservices.gpxanalyzer.ui.gpxchart.item;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.subjects.PublishSubject;

public class AdapterDataObserverImpl extends RecyclerView.AdapterDataObserver {
    private final PublishSubject<List<ChartAreaItem>> changedItems = PublishSubject.create();
    private final List<ChartAreaItem> chartAreaItemList;

    @Inject
    public AdapterDataObserverImpl(List<ChartAreaItem> chartAreaItemList) {
        this.chartAreaItemList = chartAreaItemList;
    }

    public PublishSubject<List<ChartAreaItem>> getChangedItems() {
        return changedItems;
    }


    @Override
    public void onChanged() {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onChanged() called");

        changedItems.onNext(
                chartAreaItemList
        );
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeChanged() called with: positionStart = [" + positionStart + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList.subList(positionStart, positionStart + itemCount)
        );
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeInserted() called with: positionStart = [" + positionStart + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList
        );
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeRemoved() called with: positionStart = [" + positionStart + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList
        );
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        Log.d(AdapterDataObserverImpl.class.getSimpleName(), "onItemRangeMoved() called with: fromPosition = [" + fromPosition + "], toPosition = [" + toPosition + "], itemCount = [" + itemCount + "]");
        changedItems.onNext(
                chartAreaItemList
        );
    }

}
