package com.itservices.gpxanalyzer.usecase;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.provider.GpxFileInfoProvider;
import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.ui.components.miniature.GpxFileInfoMiniatureProvider;
import com.itservices.gpxanalyzer.ui.components.miniature.MiniatureMapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Use case responsible for updating the list of available GPX files and generating their corresponding map miniatures.
 * It orchestrates the process of:
 * <ol>
 *     <li>Searching the device storage for GPX files using {@link GpxFileInfoProvider}.</li>
 *     <li>Sequentially generating a map miniature for each found file using {@link GpxFileInfoMiniatureProvider}.</li>
 *     <li>Reporting the progress of miniature generation via {@link GlobalEventWrapper}.</li>
 *     <li>Replacing the entire list in the {@link GpxFileInfoProvider} with the updated {@link GpxFileInfo} objects (which now include miniature paths).</li>
 * </ol>
 * The process runs primarily on background threads (IO, Computation) and observes final results/errors on the main thread.
 */
@Singleton
public class UpdateGpxFileInfoListUseCase {
    private static final String TAG = UpdateGpxFileInfoListUseCase.class.getSimpleName();
    /**
     * Provider for accessing and managing GPX file information.
     */
    @Inject
    GpxFileInfoProvider gpxFileInfoProvider;

    /**
     * Provider responsible for generating map miniatures for GPX files.
     */
    @Inject
    GpxFileInfoMiniatureProvider miniatureProvider;

    /**
     * Global event bus for publishing progress updates during miniature generation.
     */
    @Inject
    GlobalEventWrapper globalEventWrapper;

    /**
     * Atomically holds the last reported progress event to avoid duplicate emissions.
     */
    private AtomicReference<EventProgress> lastEventProgress = new AtomicReference<>();

    /**
     * Constructor for dependency injection.
     */
    @Inject
    public UpdateGpxFileInfoListUseCase() { }

    /**
     * Initiates the process of searching for GPX files, generating miniatures for each, and updating the provider.
     * It first searches for files using {@link GpxFileInfoProvider#searchAndParseGpxFilesRecursively(Context)}.
     * Then, it processes each found file sequentially using {@link #processSingleItem(MiniatureMapView, GpxFileInfo, List, List)}.
     * Finally, it replaces the list in the {@link GpxFileInfoProvider} with the updated items.
     * Progress is reported after each miniature is generated.
     *
     * @param context           The application context, needed for file searching.
     * @param miniatureRenderer The {@link MiniatureMapView} instance used to render the miniatures off-screen.
     * @return A {@link Completable} that completes when the entire process (search, generation, update) is finished,
     *         or errors if any step fails.
     */
    public Completable updateAndGenerateMiniatures(Context context, MiniatureMapView miniatureRenderer) {
        return gpxFileInfoProvider
                .searchAndParseGpxFilesRecursively(context)
                .flatMapCompletable(fullGpxFileInfoList -> {
                    if (fullGpxFileInfoList.isEmpty()) {
                        Log.d(TAG, "No GPX files found to process.");
                        return Completable.complete();
                    }

                    Log.d(TAG, "Starting sequential miniature generation for " + fullGpxFileInfoList.size() + " items.");

                    List<GpxFileInfo> updatedItems = new ArrayList<>(fullGpxFileInfoList.size());

                    lastEventProgress.set(EventProgress.create(GpxFileInfoMiniatureProvider.class, 0, fullGpxFileInfoList.size()));
                    globalEventWrapper.onNext(lastEventProgress.get());

                    return Observable.fromIterable(fullGpxFileInfoList)
                            .concatMapCompletable(gpxFileInfo -> processSingleItem(miniatureRenderer, gpxFileInfo, updatedItems, fullGpxFileInfoList), 1)
                            .andThen(Completable.defer(() -> {
                                Log.i(TAG, "Miniature generation complete. Replacing items in provider...");
                                return gpxFileInfoProvider.replaceAll(updatedItems);
                            }))
                            .doOnComplete(() -> {
                                Log.i(TAG, "Sequential miniature generation and provider update completed successfully.");
                            })
                            .doOnError(throwable -> Log.e(TAG, "Error during sequential processing or provider update.", throwable));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Processes a single {@link GpxFileInfo} item: generates its miniature and adds the updated item to a list.
     * It requests miniature generation via {@link GpxFileInfoMiniatureProvider#requestForGenerateMiniature(MiniatureMapView, GpxFileInfo)},
     * waits for the result (an updated GpxFileInfo with the miniature path),
     * adds the result to the `updatedList`, and reports progress.
     * Uses `concatMapCompletable` in the calling method to ensure sequential processing.
     *
     * @param miniatureRenderer  The view used for rendering.
     * @param item               The {@link GpxFileInfo} to process.
     * @param updatedList        The list to add the processed item (with miniature) to.
     * @param fullGpxFileInfoList The original full list, used for progress calculation.
     * @return A {@link Completable} that completes when the miniature is generated and the item is added to the list,
     *         or errors if miniature generation fails.
     */
    private Completable processSingleItem(MiniatureMapView miniatureRenderer, GpxFileInfo item, List<GpxFileInfo> updatedList, List<GpxFileInfo> fullGpxFileInfoList) {
        Log.d(TAG, "Requesting miniature for: " + item.file().getName());
        return miniatureProvider.requestForGenerateMiniature(miniatureRenderer, item)
                .andThen(miniatureProvider.getGpxFileInfoWithMiniature()
                        .filter(emittedItem -> emittedItem.equals(item))
                        .firstOrError()
                        .doOnSuccess(updatedItem -> {
                            updatedList.add(updatedItem);

                            Log.d(TAG, "Miniature processSingleItem called with: updatedList.size() = [" + updatedList.size() + "]");

                            lastEventProgress.set(
                                    globalEventWrapper.onNextChanged(lastEventProgress.get(),
                                            EventProgress.create(GpxFileInfoMiniatureProvider.class, updatedList.size(), fullGpxFileInfoList.size()))
                            );


                            Log.d(TAG, "Miniature completed and added for: " + updatedItem.file().getName());
                        })
                        .doOnError(throwable -> Log.e(TAG, "Error waiting for miniature completion for: " + item.file().getName(), throwable))
                )
                .ignoreElement();
    }
}

