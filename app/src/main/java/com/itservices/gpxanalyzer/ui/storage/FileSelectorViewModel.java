package com.itservices.gpxanalyzer.ui.storage;

import static com.itservices.gpxanalyzer.events.PercentageUpdateEventSourceType.MINIATURE_GENERATION_PROGRESS;
import static com.itservices.gpxanalyzer.events.PercentageUpdateEventSourceType.STORAGE_SEARCH_PROGRESS;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.data.parser.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.events.PercentageUpdateEventSourceType;
import com.itservices.gpxanalyzer.ui.components.miniature.MiniatureMapView;
import com.itservices.gpxanalyzer.usecase.GetGpxFileInfoListUseCase;
import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;
import com.itservices.gpxanalyzer.usecase.UpdateGpxFileInfoListUseCase;
import com.itservices.gpxanalyzer.utils.common.ConcurrentUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for the {@link FileSelectorFragment}.
 * Manages the state related to file selection, including permissions, the list of found GPX files,
 * and the progress of background search/processing operations.
 * Interacts with use cases to perform actions like permission checks, file searching, miniature generation,
 * and retrieving file lists.
 */
@HiltViewModel
public class FileSelectorViewModel extends ViewModel {
    private static final String TAG = FileSelectorViewModel.class.getSimpleName();
    /** LiveData indicating whether necessary file access permissions are granted. */
    private final MutableLiveData<Boolean> requestPermissionsLiveData = new MutableLiveData<>();
    /** LiveData emitting {@link EventProgress} updates for background operations (searching, miniature generation). */
    private final MutableLiveData<EventProgress> percentageEventProgressLiveData = new MutableLiveData<>(null);
    /** LiveData holding the list of found {@link GpxFileInfo} objects. */
    private final MutableLiveData<List<GpxFileInfo>> fileInfoListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> searchWasRequestedLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<FileInfoItem> selectedFileInfoItemLiveData = new MutableLiveData<>(null);

    /** Composite disposable to manage RxJava subscriptions. */
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;
    @Inject
    UpdateGpxFileInfoListUseCase updateGpxFileInfoListUseCase;
    @Inject
    GetGpxFileInfoListUseCase getGpxFileInfoListUseCase;
    @Inject
    GlobalEventWrapper globalEventWrapper;
    private Disposable disposableRequestPermissions;
    private Disposable disposableCheckAndRequestPermissions;
    private Disposable disposableSearchProgress;

    /**
     * Constructor used by Hilt for dependency injection.
     */
    @Inject
    public FileSelectorViewModel() {
    }

    /**
     * Returns the LiveData containing the list of found {@link GpxFileInfo} objects.
     *
     * @return LiveData<List<GpxFileInfo>>
     */
    public LiveData<List<GpxFileInfo>> getFoundFileListLiveData() {
        return fileInfoListLiveData;
    }

    public LiveData<Boolean> getSearchWasRequestedLiveData() {
        return searchWasRequestedLiveData;
    }

    public Boolean getSearchWasRequested() {
        return searchWasRequestedLiveData.getValue() != null && searchWasRequestedLiveData.getValue();
    }

    /**
     * Fetches the most recent list of found/processed GPX files from the {@link GetGpxFileInfoListUseCase}
     * and updates the {@link #fileInfoListLiveData}.
     * Handles potential errors during fetching.
     */
    public void receiveRecentFoundFileList() {
        Log.d(TAG, "receiveRecentFoundFileList() called");

        disposables.add(
                getGpxFileInfoListUseCase
                        .getGpxFileInfoList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(throwable -> Log.e(TAG, "Error fetching GPX file list from use case", throwable))
                        .subscribe(
                                fileList -> {
                                    fileInfoListLiveData.setValue(fileList);
                                    searchWasRequestedLiveData.setValue(false);
                                    globalEventWrapper.clearEventProgressState();
                                },
                                error -> {
                                    Log.e(TAG, "Error setting filesInfoLiveData in receiveRecentFoundFileList", error);
                                    fileInfoListLiveData.setValue(new ArrayList<>());
                                    searchWasRequestedLiveData.setValue(false);
                                    globalEventWrapper.clearEventProgressState();
                                }
                        )
        );
    }

    /**
     * Returns the LiveData emitting progress updates for background tasks.
     *
     * @return LiveData<EventProgress>
     */
    public LiveData<EventProgress> getPercentageEventProgress() {
        return percentageEventProgressLiveData;
    }

    /**
     * Initiates the background process to search for GPX files on storage and generate map miniatures for them.
     * It uses {@link UpdateGpxFileInfoListUseCase} to perform the search and generation.
     * It also sets up observers via {@link GlobalEventWrapper} to listen for progress updates
     * ({@link PercentageUpdateEventSourceType#STORAGE_SEARCH_PROGRESS} and
     * {@link PercentageUpdateEventSourceType#MINIATURE_GENERATION_PROGRESS})
     * and updates {@link #percentageEventProgressLiveData} accordingly.
     * Upon completion (success or error), it calls {@link #receiveRecentFoundFileList()} to refresh the displayed list.
     *
     * @param context           The application context.
     * @param miniatureRenderer The {@link MiniatureMapView} used for off-screen rendering of miniatures.
     */
    public void searchGpxFilesAndGenerateMiniatures(Context context, MiniatureMapView miniatureRenderer) {
        Log.d(TAG, "searchGpxFilesAndGenerateMiniatures() called with: context = [" + context + "], renderer = [" + miniatureRenderer + "]");



        percentageEventProgressLiveData.setValue(
                EventProgress.create(STORAGE_SEARCH_PROGRESS, 0)
        );

        ConcurrentUtil.tryToDispose(disposableSearchProgress);
        disposableSearchProgress = globalEventWrapper.getEventProgressFromType(STORAGE_SEARCH_PROGRESS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::accept);

        disposables.add(globalEventWrapper.getEventProgressFromType(MINIATURE_GENERATION_PROGRESS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::accept));

        disposables.add(updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(context, miniatureRenderer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.i(TAG, "GPX file search and miniature generation completed successfully.");
                            receiveRecentFoundFileList();
                        },
                        error -> {
                            Log.e(TAG, "Error searching/generating miniatures for GPX files", error);
                            receiveRecentFoundFileList();
                        }
                ));
    }

    /**
     * Returns the LiveData indicating the current permission grant status.
     *
     * @return LiveData<Boolean>
     */
    @NonNull
    public LiveData<Boolean> getPermissionsGrantedLiveData() {
        return requestPermissionsLiveData;
    }

    public LiveData<FileInfoItem> getSelectedFileInfoItemLiveData() {
        return selectedFileInfoItemLiveData;
    }

    /**
     * Gets the current value of the permission granted state.
     *
     * @return {@code true} if permissions are granted, {@code false} otherwise (or if LiveData value is null).
     */
    public Boolean getPermissionsGranted() {
        return requestPermissionsLiveData.getValue() != null ? requestPermissionsLiveData.getValue() : false;
    }

    /**
     * Notifies the {@link SelectGpxFileUseCase} that a specific file has been selected by the user.
     *
     * @param fileInfoItem contains the selected {@link File}.
     */
    public void selectFileInfoItem(FileInfoItem fileInfoItem) {
        selectGpxFileUseCase.setSelectedFile(fileInfoItem.fileInfo().file());
        selectedFileInfoItemLiveData.setValue(fileInfoItem);
    }

    /**
     * Checks if necessary file access permissions are granted and requests them if needed.
     * Uses {@link SelectGpxFileUseCase#checkAndRequestPermissions(FragmentActivity)} and updates
     * {@link #requestPermissionsLiveData} with the result (immediately if already granted, or after user interaction).
     *
     * @param requireActivity The activity context required for permission checks/requests.
     */
    public void checkAndRequestPermissions(FragmentActivity requireActivity) {
        ConcurrentUtil.tryToDispose(disposableCheckAndRequestPermissions);
        disposableCheckAndRequestPermissions = selectGpxFileUseCase.checkAndRequestPermissions(requireActivity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestPermissionsLiveData::setValue);
    }

    /**
     * Requests the {@link SelectGpxFileUseCase} to launch the system file picker.
     * (Note: This might not be used if the fragment implements its own picker logic).
     */
    public void openFilePicker() {
        selectGpxFileUseCase.openFilePicker();
    }

    /**
     * Initializes the ViewModel by subscribing to the permission status updates from {@link SelectGpxFileUseCase}.
     * This ensures the ViewModel's permission LiveData stays synchronized.
     */
    public void init() {
        ConcurrentUtil.tryToDispose(disposableRequestPermissions);
        disposableRequestPermissions = selectGpxFileUseCase.getPermissionsGranted()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestPermissionsLiveData::setValue);
    }

    /**
     * Cleans up all RxJava disposables when the ViewModel is cleared.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        ConcurrentUtil.tryToDispose(disposableRequestPermissions);
        ConcurrentUtil.tryToDispose(disposableCheckAndRequestPermissions);
        ConcurrentUtil.tryToDispose(disposableSearchProgress);
    }

    /**
     * Helper method to accept {@link EventProgress} emissions and update the LiveData.
     *
     * @param event The received progress event.
     */
    private void accept(EventProgress event) {
        //Log.d(TAG, "accept() called with: event = [" + event + "]");

        percentageEventProgressLiveData.setValue(event);
    }
}
