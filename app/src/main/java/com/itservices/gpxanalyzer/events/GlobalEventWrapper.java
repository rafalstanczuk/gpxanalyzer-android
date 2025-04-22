package com.itservices.gpxanalyzer.events;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * A singleton class acting as a central event bus for the application.
 * It utilizes RxJava Subjects (PublishSubject and BehaviorSubject) to broadcast and manage
 * various application-wide events like entry selections, request statuses, progress updates,
 * and chart visibility changes.
 *
 * This allows different components of the application to communicate indirectly by publishing
 * events to this wrapper and subscribing to the relevant event streams.
 */
@Singleton
public class GlobalEventWrapper {
    private static final String TAG = GlobalEventWrapper.class.getSimpleName();

    /**
     * Subject for broadcasting {@link EventEntrySelection} events.
     * Uses a PublishSubject, meaning subscribers only receive events emitted *after* they subscribe.
     */
    private final PublishSubject<EventEntrySelection> eventEntrySelectionPublishSubject = PublishSubject.create();

    /**
     * Atomically holds the last emitted {@link EventEntrySelection}.
     * Useful for retrieving the most recent selection state.
     */
    public final AtomicReference<EventEntrySelection> lastEventEntrySelection = new AtomicReference<>();

    /**
     * Subject for broadcasting {@link RequestStatus} events.
     * Uses a BehaviorSubject, meaning new subscribers immediately receive the *last* emitted status,
     * or a default value if none has been emitted yet.
     */
    private final BehaviorSubject<RequestStatus> requestStatusPublishSubject = BehaviorSubject.create();

    /**
     * Subject for broadcasting {@link EventProgress} events.
     * Uses a BehaviorSubject, providing the last progress update to new subscribers.
     */
    private final BehaviorSubject<EventProgress> eventProgressBehaviorSubject = BehaviorSubject.create();

    /**
     * Subject for broadcasting {@link EventVisibleChartEntriesTimestamp} events.
     * Uses a BehaviorSubject, providing the last visible timestamp range to new subscribers.
     */
    private final BehaviorSubject<EventVisibleChartEntriesTimestamp> eventVisibleChartEntriesTimestampBehaviorSubject = BehaviorSubject.create();

    /**
     * Constructs the GlobalEventWrapper. Marked with @Inject for Dagger/Hilt dependency injection.
     */
    @Inject
    public GlobalEventWrapper(){}

    /**
     * Publishes an {@link EventEntrySelection} event to the corresponding subject.
     * Also updates the {@link #lastEventEntrySelection} atomic reference.
     *
     * @param eventEntrySelection The event to publish.
     */
    public void onNext(EventEntrySelection eventEntrySelection) {
        lastEventEntrySelection.set(eventEntrySelection);
        eventEntrySelectionPublishSubject.onNext(eventEntrySelection);
    }

    /**
     * Publishes a {@link RequestStatus} event to the corresponding subject.
     *
     * @param requestStatus The event to publish.
     */
    public void onNext(RequestStatus requestStatus) {
        requestStatusPublishSubject.onNext(requestStatus);
    }

    /**
     * Publishes an {@link EventVisibleChartEntriesTimestamp} event to the corresponding subject.
     *
     * @param eventVisibleChartEntriesTimestamp The event to publish.
     */
    public void onNext(EventVisibleChartEntriesTimestamp eventVisibleChartEntriesTimestamp) {
        eventVisibleChartEntriesTimestampBehaviorSubject.onNext(eventVisibleChartEntriesTimestamp);
    }

    /**
     * Publishes an {@link EventProgress} event to the corresponding subject.
     *
     * @param eventProgress The event to publish.
     */
    public void onNext(EventProgress eventProgress) {
        eventProgressBehaviorSubject.onNext(eventProgress);
    }

    /**
     * Conditionally publishes an {@link EventProgress} event only if its percentage value has changed
     * compared to the last known progress event.
     * This helps avoid redundant progress updates.
     *
     * @param lastEventProgress    The previously known progress event.
     * @param currentEventProgress The new progress event to potentially publish.
     * @return The `currentEventProgress` if it was published, otherwise the original `lastEventProgress`.
     */
    public EventProgress onNextChanged(EventProgress lastEventProgress, EventProgress currentEventProgress) {
        if (lastEventProgress.percentage() != currentEventProgress.percentage()) {
            //Log.d(TAG, "onNextChanged() called with: lastEventProgress = [" + lastEventProgress + "], currentEventProgress = [" + currentEventProgress + "]");

            lastEventProgress = currentEventProgress;

            onNext(currentEventProgress);
        }
        return lastEventProgress;
    }

    /**
     * Provides an observable stream for {@link EventEntrySelection} events.
     *
     * @return An {@link Observable} emitting entry selection events.
     */
    public Observable<EventEntrySelection> getEventEntrySelection() {
        return eventEntrySelectionPublishSubject.hide();
    }

    /**
     * Provides an observable stream for {@link RequestStatus} events.
     *
     * @return An {@link Observable} emitting request status events.
     */
    public Observable<RequestStatus> getRequestStatus() {
        return requestStatusPublishSubject.hide();
    }

    /**
     * Provides an observable stream for {@link EventVisibleChartEntriesTimestamp} events.
     *
     * @return An {@link Observable} emitting visible chart timestamp events.
     */
    public Observable<EventVisibleChartEntriesTimestamp> getEventVisibleChartEntriesTimestamp() {
        return eventVisibleChartEntriesTimestampBehaviorSubject.hide();
    }

    /**
     * Provides an observable stream for {@link EventProgress} events, filtered by a specific source type.
     * This allows subscribers to listen for progress updates only from a particular operation.
     *
     * @param percentageUpdateEventSourceType The source type to filter progress events by.
     * @return An {@link Observable} emitting progress events matching the specified source type.
     */
    public Observable<EventProgress> getEventProgressFromType(PercentageUpdateEventSourceType percentageUpdateEventSourceType) {
        return eventProgressBehaviorSubject
                .hide()
                .subscribeOn(Schedulers.newThread())
                .filter(eventProgress -> percentageUpdateEventSourceType == eventProgress.percentageUpdateEventSourceType());
    }
}
