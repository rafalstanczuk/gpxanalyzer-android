package com.itservices.gpxanalyzer.events;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class GlobalEventWrapper {
    private static final String TAG = GlobalEventWrapper.class.getSimpleName();

    private final PublishSubject<EventEntrySelection> eventEntrySelectionPublishSubject = PublishSubject.create();

    public final AtomicReference<EventEntrySelection> lastEventEntrySelection = new AtomicReference<>();

    private final BehaviorSubject<RequestStatus> requestStatusPublishSubject = BehaviorSubject.create();

    private final BehaviorSubject<EventProgress> eventProgressBehaviorSubject = BehaviorSubject.create();

    private final BehaviorSubject<EventVisibleChartEntriesTimestamp> eventVisibleChartEntriesTimestampBehaviorSubject = BehaviorSubject.create();

    @Inject
    public GlobalEventWrapper(){}

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

    public void onNext(EventProgress eventProgress) {
        eventProgressBehaviorSubject.onNext(eventProgress);
    }

    public Observable<EventEntrySelection> getEventEntrySelection() {
        return eventEntrySelectionPublishSubject.hide();
    }

    public Observable<RequestStatus> getRequestStatus() {
        return requestStatusPublishSubject.hide();
    }

    public Observable<EventVisibleChartEntriesTimestamp> getEventVisibleChartEntriesTimestamp() {
        return eventVisibleChartEntriesTimestampBehaviorSubject.hide();
    }

    public Observable<EventProgress> getEventProgressFrom(Class<?> withSourceClass) {
        return eventProgressBehaviorSubject
                .hide()
                .subscribeOn(Schedulers.newThread())
                .filter(eventProgress -> withSourceClass.equals(eventProgress.sourceClass()));
    }
}
