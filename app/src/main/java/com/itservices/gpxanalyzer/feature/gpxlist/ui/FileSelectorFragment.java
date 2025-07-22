package com.itservices.gpxanalyzer.feature.gpxlist.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.databinding.FragmentFileSelectorBinding;
import com.itservices.gpxanalyzer.core.ui.mapper.FileInfoItemMapper;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaOAuthManager;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

/**
 * A {@link DialogFragment} that provides a user interface for selecting a GPX file from storage.
 * It displays a list of found GPX files (with details and miniatures) using a {@link RecyclerView}
 * and the {@link FileAdapter}. It handles permission requests for file access and initiates
 * a background search for GPX files, including miniature generation, displaying progress.
 * Interacts with {@link FileSelectorViewModel} for state management and business logic.
 */
@AndroidEntryPoint
public class FileSelectorFragment extends DialogFragment {

    private static final String TAG = FileSelectorFragment.class.getSimpleName();
    private static final int STRAVA_OAUTH_REQUEST_CODE = 1010;

    /**
     * Manages RxJava subscriptions to prevent memory leaks.
     */
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    /**
     * Activity Result launcher for Strava OAuth flow.
     */
    private ActivityResultLauncher<Intent> stravaOAuthLauncher;
    
    /**
     * ViewModel associated with this fragment.
     */
    private FileSelectorViewModel viewModel;
    /**
     * Adapter for the RecyclerView displaying the list of files.
     */
    private FileAdapter fileAdapter;
    /**
     * View binding instance for this fragment's layout (fragment_file_selector.xml).
     */
    private FragmentFileSelectorBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FileSelectorViewModel.class);
        viewModel.init();
        
        // Initialize Strava OAuth launcher
        initializeStravaOAuthLauncher();
        
        // Initialize Strava authentication status
        viewModel.initializeStravaAuthStatus();
    }

    /**
     * Initializes the Activity Result launcher for Strava OAuth.
     */
    private void initializeStravaOAuthLauncher() {
        stravaOAuthLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Received OAuth result: " + result.getResultCode());
                StravaOAuthManager.OAuthResult oauthResult = viewModel.handleStravaOAuthResult(
                    STRAVA_OAUTH_REQUEST_CODE, 
                    result.getResultCode(), 
                    result.getData()
                );
                handleStravaOAuthResult(oauthResult);
            }
        );
    }

    /**
     * Handles the result from Strava OAuth flow.
     */
    private void handleStravaOAuthResult(StravaOAuthManager.OAuthResult result) {
        switch (result.type) {
            case SUCCESS:
                Toast.makeText(requireContext(), "✅ Connected to Strava successfully!", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Strava OAuth successful");
                break;
            case CANCELLED:
                Toast.makeText(requireContext(), "❌ Strava connection cancelled", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Strava OAuth cancelled");
                break;
            case ERROR:
                String error = result.errorMessage != null ? result.errorMessage : "Unknown error";
                Toast.makeText(requireContext(), "❌ Strava connection failed: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Strava OAuth error: " + error);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileSelectorBinding.inflate(inflater, container, false);

        fileAdapter = new FileAdapter(viewModel, getViewLifecycleOwner());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(fileAdapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all observers
        initializeObservers();

        viewModel.getPermissionsGrantedLiveData().observe(getViewLifecycleOwner(), granted -> {
            if (!granted) {
                warningNeedsPermissions(FileSelectorFragment.this.requireActivity());
            } else {
                checkStravaAuthentication();
                initViewModelObservers();
                viewModel.receiveRecentFoundFileList();
            }
        });

        viewModel.getSelectedFileInfoItemLiveData().observe(getViewLifecycleOwner(), fileInfoItem -> {
            if (fileInfoItem != null) {
                dismiss();
            }
        });

        viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());

        binding.searchFilesButton.setOnClickListener(v -> {
            if (viewModel.getPermissionsGranted()) {
                startRecursiveSearchAndGenerateMiniatures();
            } else {
                viewModel.checkAndRequestPermissions(FileSelectorFragment.this.requireActivity());
            }
        });
    }

    /**
     * Initializes all LiveData observers for Strava authentication.
     */
    private void initializeObservers() {
        // Observe authentication status changes
        viewModel.getStravaAuthStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            Log.d(TAG, "Strava auth status: " + status);
            handleAuthenticationStatusChange(status);
        });

        // Observe authentication description changes
        viewModel.getStravaAuthDescriptionLiveData().observe(getViewLifecycleOwner(), description -> {
            Log.d(TAG, "Strava auth description: " + description);
            // Could display this in UI if needed
        });
    }

    /**
     * Handles changes to the Strava authentication status.
     */
    private void handleAuthenticationStatusChange(StravaOAuthManager.AuthenticationStatus status) {
        Log.d(TAG, "Strava auth status: " + status);
        
        switch (status) {
            case AUTHENTICATED:
                Log.i(TAG, "✅ Strava authenticated successfully - no action needed");
                break;
                
            case NOT_AUTHENTICATED:
                Log.w(TAG, "⚠️ User not authenticated with Strava");
                // Only prompt for auth if we haven't already done so
                if (!isOAuthInProgress()) {
                    Log.i(TAG, "Prompting for Strava authentication");
                    promptStravaAuthentication();
                }
                break;
                
            case TOKEN_EXPIRED:
                Log.w(TAG, "⚠️ Strava token expired - refreshing");
                viewModel.refreshStravaToken();
                break;
                
            case SCOPE_MISSING:
                Log.w(TAG, "⚠️ Strava token missing required scopes - re-authenticating");
                Toast.makeText(requireContext(), 
                    "Strava access requires additional permissions. Please reconnect.", 
                    Toast.LENGTH_LONG).show();
                promptStravaAuthentication(true);
                break;
                
            case ERROR:
                Log.e(TAG, "❌ Error with Strava authentication");
                // Don't auto-prompt on errors to avoid loops
                break;
                
            case NOT_CONFIGURED:
                Log.d(TAG, "Strava OAuth not configured - ignoring");
                break;
        }
    }

    /**
     * Checks if OAuth flow is currently in progress to avoid duplicate flows.
     */
    private boolean isOAuthInProgress() {
        // Simple check - could be enhanced with proper state tracking
        return false; // For now, allow OAuth flows
    }

    /**
     * Prompts user for Strava authentication.
     */
    private void promptStravaAuthentication() {
        promptStravaAuthentication(false);
    }
    
    /**
     * Prompts user for Strava authentication.
     * @param forceReauth true if re-authentication should be forced
     */
    private void promptStravaAuthentication(boolean forceReauth) {
        Log.i(TAG, "Prompting for Strava authentication (forceReauth=" + forceReauth + ")");
        
        // Show informative toast
        Toast.makeText(requireContext(), 
            "🔗 Connecting to Strava for enhanced features...", 
            Toast.LENGTH_SHORT).show();
            
        // Start OAuth flow
        startStravaOAuthFlow(forceReauth);
    }

    /**
     * Checks Strava authentication status and handles accordingly.
     */
    private void checkStravaAuthentication() {
        if (viewModel.isStravaOAuthConfigured()) {
            Log.d(TAG, "Strava OAuth configured, checking authentication...");
            // Status updates will be handled by LiveData observers
        } else {
            Log.d(TAG, "Strava OAuth not configured, skipping authentication check");
        }
    }

    /**
     * Starts the Strava OAuth authentication flow.
     */
    private void startStravaOAuthFlow() {
        startStravaOAuthFlow(false);
    }

    /**
     * Starts the Strava OAuth authentication flow.
     * @param forceReauth true if re-authentication should be forced
     */
    private void startStravaOAuthFlow(boolean forceReauth) {
        if (!viewModel.isStravaOAuthConfigured()) {
            Toast.makeText(requireContext(), "❌ Strava OAuth not configured", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot start OAuth - not configured");
            return;
        }

        try {
            Log.i(TAG, "Starting Strava OAuth flow... (forceReauth=" + forceReauth + ")");
            Intent oauthIntent = new Intent(requireContext(), StravaOAuthActivity.class);
            if (forceReauth) {
                oauthIntent.putExtra(StravaOAuthActivity.EXTRA_FORCE_REAUTH, true);
            }
            stravaOAuthLauncher.launch(oauthIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting Strava OAuth", e);
            Toast.makeText(requireContext(), "❌ Failed to start Strava authentication", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Public method to manually trigger Strava authentication.
     * Can be called from menu items or other UI elements.
     */
    public void triggerStravaAuthentication() {
        startStravaOAuthFlow();
    }

    /**
     * Public method to sign out from Strava.
     */
    public void signOutFromStrava() {
        viewModel.signOutFromStrava();
        Toast.makeText(requireContext(), "📤 Signed out from Strava", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "User signed out from Strava");
    }

    /**
     * Public method to get current Strava authentication status.
     */
    public boolean isStravaAuthenticated() {
        StravaOAuthManager.AuthenticationStatus currentStatus = viewModel.getStravaAuthStatusLiveData().getValue();
        return currentStatus == StravaOAuthManager.AuthenticationStatus.AUTHENTICATED;
    }

    /**
     * Public method to refresh Strava token manually.
     */
    public void refreshStravaToken() {
        Log.i(TAG, "Manually refreshing Strava token...");
        viewModel.refreshStravaToken();
        Toast.makeText(requireContext(), "🔄 Refreshing Strava connection...", Toast.LENGTH_SHORT).show();
    }



    /**
     * Starts the process of recursively searching for GPX files and generating miniatures.
     * Disables the search button, updates its text to show progress, observes progress events
     * from the ViewModel, and calls the ViewModel's method to begin the search.
     */
    private void startRecursiveSearchAndGenerateMiniatures() {
        binding.searchFilesButton.setEnabled(false);
        binding.searchFilesButton.setText(R.string.searching_files);

        viewModel.getPercentageEventProgress().observe(getViewLifecycleOwner(), eventProgress -> {
            if (eventProgress == null) {
                return;
            }

            switch (eventProgress.percentageUpdateEventSourceType()) {
                case GPX_FILE_DATA_ENTITY_PROVIDER, UNKNOWN_SOURCE -> {
                }
                case STORAGE_SEARCH_PROGRESS -> {
                    binding.searchFilesButton.setText(getString(R.string.searching_files_progress, eventProgress.percentage()));
                }
                case MINIATURE_GENERATION_PROGRESS -> {
                    binding.searchFilesButton.setText(getString(R.string.generating_miniatures_progress, eventProgress.percentage()));
                }
                case GEOCODING_PROCESSING -> {
                    binding.searchFilesButton.setText(getString(R.string.gathering_geocoded_addresses_progress, eventProgress.percentage()));
                }
                case UPDATING_RESOURCES_PROCESSING -> {
                    binding.searchFilesButton.setText(getString(R.string.updating_database_progress, eventProgress.percentage()));
                }
            }
        });

        viewModel.searchGpxFilesAndGenerateMiniatures(requireContext(), binding.sequentialMiniatureRenderer);
    }

    /**
     * Initializes observers for the ViewModel's LiveData, specifically the list of found GPX files.
     * Updates the {@link FileAdapter} when the list changes and re-enables the search button.
     * Shows appropriate Toast messages based on whether files were found.
     */
    private void initViewModelObservers() {
        viewModel.getFoundFileListLiveData().observe(getViewLifecycleOwner(), gpxFileInfoList -> {
            binding.searchFilesButton.setEnabled(true);
            binding.searchFilesButton.setText(R.string.search_for_file);

            if (gpxFileInfoList == null) {
                if (viewModel.getSearchWasRequested()) {
                    Toast.makeText(requireContext(), R.string.no_gpx_files_found, Toast.LENGTH_SHORT).show();
                }
            } else if (gpxFileInfoList.isEmpty()) {
                if (viewModel.getSearchWasRequested()) {
                    Toast.makeText(requireContext(), R.string.no_gpx_files_found, Toast.LENGTH_SHORT).show();
                }
                fileAdapter.setFiles(FileInfoItemMapper.mapFrom(gpxFileInfoList));
            } else {
                if (viewModel.getSearchWasRequested()) {
                    Toast.makeText(requireContext(), getString(R.string.found_gpx_files, gpxFileInfoList.size()), Toast.LENGTH_SHORT).show();
                }
                fileAdapter.setFiles(FileInfoItemMapper.mapFrom(gpxFileInfoList));
            }
        });
    }

    /**
     * Handles the case where necessary file access permissions were denied.
     * Dismisses the dialog and shows a warning Toast message.
     *
     * @param context The application context.
     */
    private void warningNeedsPermissions(Context context) {
        dismiss();
        Toast.makeText(context, R.string.permission_denied_cannot_access_files, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cleans up resources when the view is destroyed.
     * Clears the ViewBinding reference and disposes of any active RxJava subscriptions.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.clear();
    }
}
