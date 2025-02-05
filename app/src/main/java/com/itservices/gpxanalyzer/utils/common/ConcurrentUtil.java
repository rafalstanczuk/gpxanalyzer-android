package com.itservices.gpxanalyzer.utils.common;

import io.reactivex.disposables.Disposable;

public class ConcurrentUtil {
    public static void tryToDispose(Disposable disposable) {
        if (disposable!=null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
