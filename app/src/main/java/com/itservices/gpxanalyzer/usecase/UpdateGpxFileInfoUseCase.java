package com.itservices.gpxanalyzer.usecase;

import android.util.Log;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.provider.GpxFileInfoProvider;
import com.itservices.gpxanalyzer.ui.storage.FileInfoItem;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Use case responsible for updating individual {@link GpxFileInfo} records in the data provider.
 * It provides a method to directly update a single record and another method to observe a stream
 * of {@link FileInfoItem}s (likely from the UI) and trigger updates whenever an item is emitted.
 */
@Singleton
public class UpdateGpxFileInfoUseCase {
    /**
     * Provider for accessing and managing GPX file information.
     */
    @Inject
    GpxFileInfoProvider gpxFileInfoProvider;
    private final String TAG = UpdateGpxFileInfoUseCase.class.getSimpleName();

    /**
     * Manages subscriptions, particularly the one observing {@link FileInfoItem} changes.
     */
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * Constructor for dependency injection.
     */
    @Inject
    public UpdateGpxFileInfoUseCase() {
    }

    protected void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        this.compositeDisposable = compositeDisposable;
    }

    /**
     * Updates a single {@link GpxFileInfo} record in the data provider.
     * Delegates the update operation to {@link GpxFileInfoProvider#updateGpxFile(GpxFileInfo)}.
     *
     * @param gpxFileInfo The {@link GpxFileInfo} object containing the updated data.
     * @return A {@link Completable} that completes when the update operation in the provider finishes,
     *         or errors if the update fails.
     */
    public Completable updateFileInfo(GpxFileInfo gpxFileInfo) {
        Log.d(TAG, "updateFileInfo() called with: gpxFileInfo = [" + gpxFileInfo + "]");
        return gpxFileInfoProvider
                .updateGpxFile(gpxFileInfo);
    }

    /**
     * Subscribes to an {@link Observable} stream of {@link FileInfoItem}s.
     * For each emitted {@code FileInfoItem}, it extracts the {@link GpxFileInfo} and triggers
     * an update using {@link #updateFileInfo(GpxFileInfo)}.
     * The observation and update operations are performed on background threads (Schedulers.newThread(), Schedulers.io()).
     * Errors during the update are logged.
     * The subscription is added to a {@link CompositeDisposable} for proper lifecycle management.
     *
     * @param fileInfoItemObservable The stream of {@link FileInfoItem}s to observe for updates.
     */
    public void observe(Observable<FileInfoItem> fileInfoItemObservable) {
        compositeDisposable.add(
                fileInfoItemObservable
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.io())
                        .flatMapCompletable(fileInfoItem -> {
                            Log.d(TAG, "flatMapCompletable: Updating GpxFileInfo: " + fileInfoItem);
                            return updateFileInfo(fileInfoItem.fileInfo())
                                    .doOnError(throwable -> Log.e(TAG, "Error updating GpxFileInfo for: " + fileInfoItem.fileInfo().file().getName(), throwable))
                                    .onErrorComplete();
                        })
                        .subscribe(
                                () -> Log.d(TAG, "Update operation completed successfully."), // onComplete
                                throwable -> Log.e(TAG, "Error occurred during update process.", throwable) // onError
                        )
        );
    }
}

