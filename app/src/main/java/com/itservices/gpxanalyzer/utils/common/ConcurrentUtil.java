package com.itservices.gpxanalyzer.utils.common;

import io.reactivex.disposables.Disposable;

/**
 * Utility class providing helper methods for concurrent operations, particularly with RxJava.
 */
public class ConcurrentUtil {
    /**
     * Safely disposes an RxJava {@link Disposable} if it is not null and not already disposed.
     * Prevents potential NullPointerExceptions and redundant dispose calls.
     *
     * @param disposable The {@link Disposable} to potentially dispose.
     */
    public static void tryToDispose(Disposable disposable) {
        if (disposable!=null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
