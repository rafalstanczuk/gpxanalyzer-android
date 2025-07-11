package com.itservices.gpxanalyzer.core.utils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A lifecycle-aware observable that sends only new updates after subscription,
 * used for events like navigation and Snackbar messages.
 * <p>
 * This avoids a common problem with LiveData: if an observer is added after an event has been set,
 * it will typically receive the last value immediately. SingleLiveEvent prevents this behavior,
 * ensuring that an event is only delivered once to an active observer.
 * <p>
 * Note: Only one observer is supported.
 * If multiple observers need to handle the event, consider using a different pattern.
 *
 * @param <T> The type of the event data.
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {

    /**
     * Tracks whether there is a pending event that hasn't been consumed.
     */
    private final AtomicBoolean pending = new AtomicBoolean(false);

    /**
     * Sets the value of the event and marks it as pending.
     * Must be called on the main thread.
     *
     * @param value The new value for the event.
     */
    @MainThread
    public void setValue(T value) {
        pending.set(true);
        super.setValue(value);
    }

    /**
     * Observes the LiveData, but only calls the observer's `onChanged` method
     * if there is a pending event. Once consumed, the event is marked as not pending.
     *
     * @param owner    The LifecycleOwner which controls the observer.
     * @param observer The observer that will receive the event.
     */
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        super.observe(owner, t -> {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });
    }

    /**
     * Used for events without data. Sets the value to null and marks it as pending.
     * Must be called on the main thread.
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}