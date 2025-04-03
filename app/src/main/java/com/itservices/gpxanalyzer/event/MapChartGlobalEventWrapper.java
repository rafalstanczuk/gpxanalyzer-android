package com.itservices.gpxanalyzer.event;

import com.itservices.gpxanalyzer.chart.RequestStatus;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class MapChartGlobalEventWrapper {
    private static final String TAG = MapChartGlobalEventWrapper.class.getSimpleName();

    private final PublishSubject<EventEntrySelection> eventEntrySelectionPublishSubject = PublishSubject.create();

    public final AtomicReference<EventEntrySelection> lastEventEntrySelection = new AtomicReference<>();

    private final BehaviorSubject<RequestStatus> requestStatusPublishSubject = BehaviorSubject.create();

    private final BehaviorSubject<EventVisibleChartEntriesTimestamp> eventVisibleChartEntriesTimestampBehaviorSubject = BehaviorSubject.create();

    @Inject
    public MapChartGlobalEventWrapper(){}

    public void onNext(EventEntrySelection eventEntrySelection) {
        lastEventEntrySelection.set(eventEntrySelection);
        eventEntrySelectionPublishSubject.onNext(eventEntrySelection);
    }

    public void onNext(RequestStatus requestStatus) {
        requestStatusPublishSubject.onNext(requestStatus);
    }

    public void onNext(EventVisibleChartEntriesTimestamp eventVisibleChartEntriesTimestamp) {
        eventVisibleChartEntriesTimestampBehaviorSubject.onNext(eventVisibleChartEntriesTimestamp);
    }

    public Observable<EventEntrySelection> getEventEntrySelection() {
        return eventEntrySelectionPublishSubject;
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatusPublishSubject;
    }

    public Observable<EventVisibleChartEntriesTimestamp> getEventVisibleChartEntriesTimestamp() {
        return eventVisibleChartEntriesTimestampBehaviorSubject;
    }
}
