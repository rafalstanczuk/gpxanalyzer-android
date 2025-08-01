package com.itservices.gpxanalyzer.feature.gpxlist.ui;

import static com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType.GEOCODING_PROCESSING;
import static com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType.MINIATURE_GENERATION_PROGRESS;
import static com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType.STORAGE_SEARCH_PROGRESS;
import static com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType.UPDATING_RESOURCES_PROCESSING;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.core.events.EventProgress;
import com.itservices.gpxanalyzer.core.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType;
import com.itservices.gpxanalyzer.core.ui.components.miniature.MiniatureMapView;
import com.itservices.gpxanalyzer.feature.gpxlist.domain.GetGpxFileInfoListUseCase;
import com.itservices.gpxanalyzer.feature.gpxlist.domain.SelectGpxFileUseCase;
import com.itservices.gpxanalyzer.feature.gpxlist.domain.UpdateGpxFileInfoListUseCase;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaOAuthManager;
import com.itservices.gpxanalyzer.core.utils.common.ConcurrentUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final List<PercentageUpdateEventSourceType> EVENT_PROGRESS_TYPE_TO_HANDLE = Arrays.asList(
            STORAGE_SEARCH_PROGRESS,
            MINIATURE_GENERATION_PROGRESS,
            GEOCODING_PROCESSING,
            UPDATING_RESOURCES_PROCESSING);

    /** LiveData indicating whether necessary file access permissions are granted. */
    private final MutableLiveData<Boolean> requestPermissionsLiveData = new MutableLiveData<>();
    /** LiveData emitting {@link EventProgress} updates for background operations (searching, miniature generation). */
    private final MutableLiveData<EventProgress> percentageEventProgressLiveData = new MutableLiveData<>(null);
    /** LiveData holding the list of found {@link GpxFileInfo} objects. */
    private final MutableLiveData<List<GpxFileInfo>> fileInfoListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> searchWasRequestedLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<FileInfoItem> selectedFileInfoItemLiveData = new MutableLiveData<>(null);
    /** LiveData for Strava authentication status */
    private final MutableLiveData<StravaOAuthManager.AuthenticationStatus> stravaAuthStatusLiveData = new MutableLiveData<>(StravaOAuthManager.AuthenticationStatus.NOT_CONFIGURED);
    /** LiveData for Strava authentication status description */
    private final MutableLiveData<String> stravaAuthDescriptionLiveData = new MutableLiveData<>("Not connected to Strava");

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
    @Inject
    StravaOAuthManager stravaOAuthManager;
    private Disposable disposableRequestPermissions;
    private Disposable disposableCheckAndRequestPermissions;

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

        disposables.clear();
        disposables.add(globalEventWrapper.getEventProgressFromTypes(EVENT_PROGRESS_TYPE_TO_HANDLE)
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
     * If permissions are granted, also checks Strava authentication status.
     *
     * @param requireActivity The activity context required for permission checks/requests.
     */
    public void checkAndRequestPermissions(FragmentActivity requireActivity) {
        Log.d(TAG, "🔒 Checking storage permissions...");
        ConcurrentUtil.tryToDispose(disposableCheckAndRequestPermissions);
        disposableCheckAndRequestPermissions = selectGpxFileUseCase.checkAndRequestPermissions(requireActivity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(granted -> {
                    if (granted) {
                        Log.i(TAG, "✅ Storage permissions GRANTED");
                        // If permissions granted, check Strava auth status
                        updateStravaAuthenticationStatus();
                    } else {
                        Log.w(TAG, "❌ Storage permissions DENIED");
                    }
                })
                .doOnError(error -> {
                    Log.e(TAG, "💥 Error during permission check", error);
                })
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

    // Strava OAuth Methods

    /**
     * Returns LiveData for Strava authentication status.
     */
    public LiveData<StravaOAuthManager.AuthenticationStatus> getStravaAuthStatusLiveData() {
        return stravaAuthStatusLiveData;
    }

    /**
     * Returns LiveData for Strava authentication status description.
     */
    public LiveData<String> getStravaAuthDescriptionLiveData() {
        return stravaAuthDescriptionLiveData;
    }

    /**
     * Checks if OAuth is properly configured.
     */
    public boolean isStravaOAuthConfigured() {
        return stravaOAuthManager.isOAuthConfigured();
    }

    /**
     * Starts the Strava OAuth flow from the given activity.
     * Note: This method is deprecated in favor of using Activity Result API directly in the Fragment.
     * Use triggerStravaAuthentication() in the Fragment instead.
     */
    @Deprecated
    public void startStravaOAuth(@NonNull Activity activity, int requestCode) {
        stravaOAuthManager.startOAuthFlow(activity, requestCode);
    }

    /**
     * Checks if OAuth is properly configured and user authentication is needed.
     * Returns true if OAuth flow should be triggered.
     */
    public boolean shouldTriggerStravaOAuth() {
        if (!isStravaOAuthConfigured()) {
            return false;
        }
        
        StravaOAuthManager.AuthenticationStatus currentStatus = stravaAuthStatusLiveData.getValue();
        return currentStatus == StravaOAuthManager.AuthenticationStatus.NOT_AUTHENTICATED ||
               currentStatus == StravaOAuthManager.AuthenticationStatus.TOKEN_EXPIRED;
    }

    /**
     * Gets the current authentication status synchronously (for immediate checks).
     */
    public StravaOAuthManager.AuthenticationStatus getCurrentAuthStatus() {
        StravaOAuthManager.AuthenticationStatus status = stravaAuthStatusLiveData.getValue();
        return status != null ? status : StravaOAuthManager.AuthenticationStatus.NOT_AUTHENTICATED;
    }

    /**
     * Handles OAuth activity result.
     */
    public StravaOAuthManager.OAuthResult handleStravaOAuthResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        StravaOAuthManager.OAuthResult result = stravaOAuthManager.handleOAuthResult(requestCode, resultCode, data);
        
        // Update authentication status after OAuth completion
        updateStravaAuthenticationStatus();
        
        return result;
    }

    /**
     * Signs out from Strava by clearing tokens.
     */
    public void signOutFromStrava() {
        stravaOAuthManager.signOut();
        updateStravaAuthenticationStatus();
    }

    /**
     * Refreshes the current Strava token.
     */
    public void refreshStravaToken() {
        disposables.add(
            stravaOAuthManager.refreshToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    success -> {
                        if (success) {
                            Log.i(TAG, "Strava token refreshed successfully");
                        } else {
                            Log.w(TAG, "Strava token refresh failed");
                        }
                        updateStravaAuthenticationStatus();
                    },
                    error -> {
                        Log.e(TAG, "Error refreshing Strava token", error);
                        updateStravaAuthenticationStatus();
                    }
                )
        );
    }

    /**
     * Updates the Strava authentication status LiveData.
     * This is called after storage permissions are granted.
     */
    public void updateStravaAuthenticationStatus() {
        Log.d(TAG, "🔄 Updating Strava authentication status...");
        disposables.add(
            stravaOAuthManager.getAuthenticationStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    status -> {
                        stravaAuthStatusLiveData.setValue(status);
                        Log.d(TAG, "🔑 Strava auth status updated: " + status);
                    },
                    error -> {
                        Log.e(TAG, "💥 Error getting Strava auth status", error);
                        stravaAuthStatusLiveData.setValue(StravaOAuthManager.AuthenticationStatus.ERROR);
                    }
                )
        );

        disposables.add(
            stravaOAuthManager.getAuthenticationStatusDescription()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    description -> {
                        stravaAuthDescriptionLiveData.setValue(description);
                        Log.d(TAG, "📝 Strava auth description updated: " + description);
                    },
                    error -> {
                        Log.e(TAG, "💥 Error getting Strava auth description", error);
                        stravaAuthDescriptionLiveData.setValue("Error checking Strava connection");
                    }
                )
        );
    }

    /**
     * Initializes Strava authentication status when ViewModel is created.
     * Call this from the Fragment/Activity onCreate or onResume.
     */
    public void initializeStravaAuthStatus() {
        updateStravaAuthenticationStatus();
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
