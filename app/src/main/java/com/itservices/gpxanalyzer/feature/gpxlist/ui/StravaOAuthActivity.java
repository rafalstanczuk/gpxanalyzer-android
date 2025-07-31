package com.itservices.gpxanalyzer.feature.gpxlist.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaApiService;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaOAuthHelper;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaTokenManager;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.StravaOAuthManager;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.AuthorizationCodeRequest;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.StravaScope;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model.TokenResponse;
import com.itservices.gpxanalyzer.BuildConfig;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity handling Strava OAuth 2.0 authorization flow.
 * 
 * This activity:
 * 1. Shows authorization UI to user
 * 2. Opens browser for Strava authorization 
 * 3. Handles redirect with authorization code
 * 4. Exchanges code for access/refresh tokens
 * 5. Saves tokens and returns result
 */
@AndroidEntryPoint
public class StravaOAuthActivity extends AppCompatActivity {
    
    private static final String TAG = StravaOAuthActivity.class.getSimpleName();
    
    // Intent extras
    public static final String EXTRA_OAUTH_RESULT = "oauth_result";
    public static final String EXTRA_ERROR_MESSAGE = "error_message";
    public static final String EXTRA_FORCE_REAUTH = "force_reauth";
    
    // OAuth result codes
    public static final int RESULT_SUCCESS = RESULT_OK;
    public static final int RESULT_ERROR = RESULT_FIRST_USER;
    public static final int RESULT_CANCELLED = RESULT_CANCELED;
    
    @Inject
    StravaApiService stravaApiService;
    
    @Inject
    StravaTokenManager tokenManager;
    
    @Inject
    StravaOAuthManager stravaOAuthManager;
    
    private CompositeDisposable disposables = new CompositeDisposable();
    
    // UI Components
    private Button authorizeButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private TextView instructionsText;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava_oauth);
        
        initializeViews();
        setupClickListeners();

        boolean forceReauth = getIntent().getBooleanExtra(EXTRA_FORCE_REAUTH, false);

        if (!forceReauth) {
            // Check if user is already authenticated before starting OAuth flow
            checkExistingAuthentication();
        } else {
            Log.i(TAG, "Forcing re-authentication, skipping existing token check");
        }

        // Handle incoming authorization response
        handleIncomingIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }
    
    private void initializeViews() {
        authorizeButton = findViewById(R.id.btn_authorize_strava);
        progressBar = findViewById(R.id.progress_oauth);
        statusText = findViewById(R.id.tv_oauth_status);
        instructionsText = findViewById(R.id.tv_oauth_instructions);
        
        // Set initial state
        showInitialState();
    }
    
    private void setupClickListeners() {
        authorizeButton.setOnClickListener(v -> startStravaAuthorization());
    }
    
    private void showInitialState() {
        progressBar.setVisibility(View.GONE);
        authorizeButton.setVisibility(View.VISIBLE);
        authorizeButton.setText("Authorize with Strava");
        statusText.setText("Connect your Strava account");
        instructionsText.setText("Authorize GPX Analyzer to access your Strava activities.\n\nRequired permissions:\n• Read profile\n• Read activities");
    }
    
    private void showAuthorizingState() {
        progressBar.setVisibility(View.VISIBLE);
        authorizeButton.setVisibility(View.GONE);
        statusText.setText("Authorizing...");
        instructionsText.setText("Please complete authorization in your browser");
    }
    
    private void showProcessingState() {
        progressBar.setVisibility(View.VISIBLE);
        authorizeButton.setVisibility(View.GONE);
        statusText.setText("Processing...");
        instructionsText.setText("Exchanging authorization code for access tokens");
    }
    
    private void showSuccessState() {
        progressBar.setVisibility(View.GONE);
        authorizeButton.setVisibility(View.GONE);
        statusText.setText("✅ Authorization successful!");
        instructionsText.setText("You can now access your Strava activities in GPX Analyzer");
        
        // Return success after short delay
        statusText.postDelayed(() -> {
            setResult(RESULT_SUCCESS);
            finish();
        }, 2000);
    }
    
    private void showErrorState(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        authorizeButton.setVisibility(View.VISIBLE);
        authorizeButton.setText("Try Again");
        statusText.setText("❌ Authorization failed");
        instructionsText.setText("Error: " + errorMessage + "\n\nYou can try again or use the back button to cancel.");
        
        // Add manual code entry button
        Button manualCodeButton = findViewById(R.id.btn_manual_code);
        if (manualCodeButton != null) {
            manualCodeButton.setVisibility(View.VISIBLE);
            manualCodeButton.setOnClickListener(v -> showManualCodeEntryDialog());
        } else {
            Log.w(TAG, "Manual code button not found in layout");
        }
        
        // Set error result for ViewModel integration
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        setResult(RESULT_ERROR, resultIntent);
    }

    /**
     * Checks if user is already authenticated and finishes activity if so.
     */
    private void checkExistingAuthentication() {
        // Check authentication status asynchronously 
        disposables.add(
            stravaOAuthManager.isAuthenticated()
                .observeOn(AndroidSchedulers.mainThread()) // Ensure UI updates happen on main thread
                .subscribe(
                    isAuthenticated -> {
                        if (isAuthenticated) {
                            Log.i(TAG, "User already authenticated, finishing OAuth activity");
                            showSuccessState();
                        } else {
                            Log.d(TAG, "User not authenticated, showing OAuth interface");
                            showInitialState();
                        }
                    },
                    error -> {
                        Log.w(TAG, "Error checking authentication status, showing OAuth interface", error);
                        showInitialState();
                    }
                )
        );
    }
    
    /**
     * Starts the Strava authorization process by opening browser
     */
    private void startStravaAuthorization() {
        if (!tokenManager.isOAuthConfigured()) {
            String errorMessage = "OAuth not configured. Please check app configuration.";
            showErrorState(errorMessage);
            // Auto-finish on configuration error since user can't fix this
            finish();
            return;
        }
        
        try {
            boolean forceReauth = getIntent().getBooleanExtra(EXTRA_FORCE_REAUTH, false);
            String authUrl = tokenManager.getAuthorizationUrl(StravaApiService.DEFAULT_SCOPES, forceReauth);
            Log.i(TAG, "Opening authorization URL: " + authUrl);
            
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
            
            // Debug: Check what apps can handle this intent
            debugAvailableBrowsers(browserIntent);
            
            // Try to find a browser app that can handle the URL
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
                showAuthorizingState();
            } else {
                // Try alternative browser launching methods
                if (tryAlternativeBrowserLaunch(authUrl)) {
                    showAuthorizingState();
                } else {
                    handleNoBrowserFound();
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting authorization", e);
            String errorMessage = "Failed to open authorization page: " + e.getMessage();
            showErrorState(errorMessage);
        }
    }

    /**
     * Tries alternative methods to launch browsers when standard detection fails.
     */
    private boolean tryAlternativeBrowserLaunch(String authUrl) {
        Log.d(TAG, "Attempting alternative browser launch methods");
        
        // Method 1: Try Chrome specifically
        try {
            Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
            chromeIntent.setPackage("com.android.chrome");
            if (chromeIntent.resolveActivity(getPackageManager()) != null) {
                Log.i(TAG, "Launching with Chrome specifically");
                startActivity(chromeIntent);
                return true;
            } else {
                Log.d(TAG, "Chrome not available for direct launch");
            }
        } catch (Exception e) {
            Log.w(TAG, "Chrome specific launch failed: " + e.getMessage(), e);
        }

        // Method 2: Try browser chooser
        try {
            Intent chooserIntent = Intent.createChooser(
                new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)), 
                "Choose browser for Strava authorization"
            );
            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                Log.i(TAG, "Launching with browser chooser");
                startActivity(chooserIntent);
                return true;
            } else {
                Log.d(TAG, "Browser chooser not available");
            }
        } catch (Exception e) {
            Log.w(TAG, "Browser chooser failed: " + e.getMessage(), e);
        }

        // Method 3: Try common browser packages
        String[] browserPackages = {
            "com.android.chrome",           // Chrome
            "com.android.browser",          // Stock browser
            "org.mozilla.firefox",          // Firefox
            "com.opera.browser",            // Opera
            "com.microsoft.emmx",           // Edge
            "com.sec.android.app.sbrowser", // Samsung Browser
            "com.brave.browser",            // Brave
            "com.UCMobile.intl",            // UC Browser
            "com.android.htmlviewer"        // Basic HTML viewer
        };

        for (String packageName : browserPackages) {
            try {
                Intent packageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                packageIntent.setPackage(packageName);
                if (packageIntent.resolveActivity(getPackageManager()) != null) {
                    Log.i(TAG, "Launching with browser: " + packageName);
                    startActivity(packageIntent);
                    return true;
                } else {
                    Log.d(TAG, "Browser " + packageName + " not available");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error launching " + packageName + ": " + e.getMessage());
            }
        }

        // Method 4: Try WebView fallback for emulators without browsers
        try {
            Log.d(TAG, "Attempting WebView fallback for authorization");
            Intent webViewIntent = new Intent(this, StravaWebViewActivity.class);
            webViewIntent.putExtra(StravaWebViewActivity.EXTRA_AUTH_URL, authUrl);
            if (webViewIntent.resolveActivity(getPackageManager()) != null) {
                Log.i(TAG, "Launching with WebView fallback");
                startActivity(webViewIntent);
                return true;
            } else {
                Log.d(TAG, "WebView fallback activity not available");
            }
        } catch (Exception e) {
            Log.w(TAG, "WebView fallback failed: " + e.getMessage(), e);
        }
        
        // Method 5: Try system browser without specific package
        try {
            Intent genericIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
            genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            genericIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            Log.i(TAG, "Attempting generic browser launch as last resort");
            startActivity(genericIntent);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Generic browser launch failed: " + e.getMessage(), e);
        }

        Log.w(TAG, "All alternative browser launch methods failed");
        return false;
    }

    /**
     * Debug method to log available browsers and their capabilities.
     */
    private void debugAvailableBrowsers(Intent browserIntent) {
        try {
            // Check all apps that can handle VIEW intents with http/https
            Intent testIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            var activities = getPackageManager().queryIntentActivities(testIntent, 0);
            
            Log.d(TAG, "=== Available Browser Apps ===");
            Log.d(TAG, "Found " + activities.size() + " apps that can handle web URLs:");
            
            for (var activity : activities) {
                String packageName = activity.activityInfo.packageName;
                String appName = activity.activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                Log.d(TAG, "- " + appName + " (" + packageName + ")");
            }
            
            // Check specifically for our OAuth URL
            var oauthActivities = getPackageManager().queryIntentActivities(browserIntent, 0);
            Log.d(TAG, "=== Apps for OAuth URL ===");
            Log.d(TAG, "Found " + oauthActivities.size() + " apps that can handle OAuth URL");
            
            for (var activity : oauthActivities) {
                String packageName = activity.activityInfo.packageName;
                String appName = activity.activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                Log.d(TAG, "- " + appName + " (" + packageName + ")");
            }
            
            // Test Chrome specifically
            Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            chromeIntent.setPackage("com.android.chrome");
            boolean chromeAvailable = chromeIntent.resolveActivity(getPackageManager()) != null;
            Log.d(TAG, "Chrome specifically available: " + chromeAvailable);
            
            // Additional debug information about the device and environment
            Log.d(TAG, "=== Device & Environment Info ===");
            Log.d(TAG, "Android SDK: " + android.os.Build.VERSION.SDK_INT);
            Log.d(TAG, "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
            Log.d(TAG, "Product: " + android.os.Build.PRODUCT);
            
            // Check if running on emulator
            boolean isEmulator = android.os.Build.PRODUCT.contains("sdk") || 
                                android.os.Build.MODEL.contains("Emulator") ||
                                android.os.Build.MODEL.contains("Android SDK");
            Log.d(TAG, "Running on emulator: " + isEmulator);
            
            // Check WebView availability
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // getCurrentWebViewPackage requires API 26 (Android 8.0+)
                    android.content.pm.PackageInfo webViewPackageInfo = android.webkit.WebView.getCurrentWebViewPackage();
                    String webViewPackage = webViewPackageInfo.packageName; // Use packageName field directly
                    String webViewVersion = webViewPackageInfo.versionName;
                    Log.d(TAG, "WebView package: " + webViewPackage + " (version: " + webViewVersion + ")");
                } else {
                    // For older versions, just log that we can't get the package info
                    Log.d(TAG, "WebView package info not available (requires API 26+)");
                    // Try to detect if WebView is available in another way
                    try {
                        new android.webkit.WebView(this);
                        Log.d(TAG, "WebView appears to be available (instance created successfully)");
                    } catch (Exception e) {
                        Log.d(TAG, "WebView creation failed: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "WebView not available: " + e.getMessage());
            }
            
            // Log intent details
            Log.d(TAG, "=== OAuth Intent Details ===");
            Log.d(TAG, "Action: " + browserIntent.getAction());
            Log.d(TAG, "Data URI: " + browserIntent.getDataString());
            Log.d(TAG, "Categories: " + (browserIntent.getCategories() != null ? browserIntent.getCategories().toString() : "null"));
            Log.d(TAG, "Type: " + browserIntent.getType());
            Log.d(TAG, "Package: " + browserIntent.getPackage());
            
            // Check if we have browser permissions
            boolean hasInternetPermission = checkSelfPermission(android.Manifest.permission.INTERNET) == 
                                          android.content.pm.PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Has INTERNET permission: " + hasInternetPermission);
            
        } catch (Exception e) {
            Log.e(TAG, "Error debugging browsers", e);
            Log.e(TAG, "Exception type: " + e.getClass().getName());
            Log.e(TAG, "Exception message: " + e.getMessage());
            Log.e(TAG, "Stack trace: ", e);
        }
    }

    /**
     * Handles the case when no browser app is found to handle authorization.
     * Provides helpful guidance especially for emulator users.
     */
    private void handleNoBrowserFound() {
        Log.w(TAG, "No browser app found to handle Strava authorization");
        
        // Check if WebView fallback is available
        Intent webViewIntent = new Intent(this, StravaWebViewActivity.class);
        String authUrl = tokenManager.getAuthorizationUrl(StravaApiService.DEFAULT_SCOPES);
        webViewIntent.putExtra(StravaWebViewActivity.EXTRA_AUTH_URL, authUrl);
        
        if (webViewIntent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "WebView fallback is available, launching it");
            Toast.makeText(this, "Using internal browser for authorization", Toast.LENGTH_LONG).show();
            startActivity(webViewIntent);
            return;
        }
        
        // Generate the authorization URL for manual use
        String manualAuthUrl = StravaOAuthHelper.generateAuthorizationUrlForTesting();
        
        String errorMessage = "⚠️ No browser app found on this device\n\n" +
                "🖥️ EMULATOR USERS - Manual Authorization:\n" +
                "1. Copy this URL to your desktop browser:\n\n" +
                manualAuthUrl + "\n\n" +
                "2. Complete Strava authorization in browser\n" +
                "3. Copy the authorization code from the redirect\n" +
                "4. Return to app - it will detect the code\n\n" +
                "🔧 ALTERNATIVE SOLUTIONS:\n" +
                "• Use emulator with Google Play Services\n" +
                "• Install Chrome browser on emulator\n" +
                "• Test on a real device with browser\n\n" +
                "📋 URL copied to clipboard for convenience!";
        
        // Copy URL to clipboard for easy access
        try {
            android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = 
                android.content.ClipData.newPlainText("Strava OAuth URL", manualAuthUrl);
            clipboard.setPrimaryClip(clip);
            Log.i(TAG, "Authorization URL copied to clipboard");
        } catch (Exception e) {
            Log.w(TAG, "Failed to copy URL to clipboard", e);
        }
        
        // Add debug info to logs
        Log.w(TAG, "=== BROWSER TROUBLESHOOTING INFO ===");
        Log.w(TAG, "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
        Log.w(TAG, "Android version: " + android.os.Build.VERSION.RELEASE + " (SDK " + android.os.Build.VERSION.SDK_INT + ")");
        Log.w(TAG, "Is emulator: " + (android.os.Build.PRODUCT.contains("sdk") || android.os.Build.MODEL.contains("Emulator")));
        Log.w(TAG, "App version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        
        showErrorState(errorMessage);
        
        // Also show the URL in a more prominent way in logs
        Log.w(TAG, "=== MANUAL AUTHORIZATION REQUIRED ===");
        Log.w(TAG, "No browser found. Copy this URL to desktop browser:");
        Log.w(TAG, manualAuthUrl);
        Log.w(TAG, "After authorization, return to app");
        Log.w(TAG, "=====================================");
    }
    
    /**
     * Handles incoming intent, checking for authorization response
     */
    private void handleIncomingIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }
        
        Uri data = intent.getData();
        Log.d(TAG, "Received redirect URI: " + data.toString());
        
        // Handle only localhost redirects from Strava
        if (data.getScheme() != null && data.getScheme().equals("http") && "localhost".equals(data.getHost())) {
            handleAuthorizationResponse(data);
        } else {
            Log.w(TAG, "Received unsupported redirect URI: " + data.toString());
        }
    }
    
    /**
     * Processes the authorization response from Strava
     */
    private void handleAuthorizationResponse(Uri responseUri) {
        String authorizationCode = responseUri.getQueryParameter("code");
        String error = responseUri.getQueryParameter("error");
        String errorDescription = responseUri.getQueryParameter("error_description");
        
        if (error != null) {
            Log.e(TAG, "Authorization error: " + error + " - " + errorDescription);
            String errorMessage = "Authorization denied";
            if (errorDescription != null && !errorDescription.isEmpty()) {
                errorMessage += ": " + errorDescription;
            } else if (!error.equals("access_denied")) {
                errorMessage += ": " + error;
            }
            showErrorState(errorMessage);
            return;
        }
        
        if (authorizationCode == null || authorizationCode.isEmpty()) {
            Log.e(TAG, "No authorization code received");
            showErrorState("Invalid authorization response from Strava");
            return;
        }
        
        Log.i(TAG, "Received authorization code, exchanging for tokens...");
        exchangeCodeForTokens(authorizationCode);
    }
    
    /**
     * Exchanges authorization code for access and refresh tokens
     */
    private void exchangeCodeForTokens(String authorizationCode) {
        showProcessingState();
        
        AuthorizationCodeRequest request = new AuthorizationCodeRequest(
            BuildConfig.STRAVA_CLIENT_ID,
            BuildConfig.STRAVA_CLIENT_SECRET,
            authorizationCode
        );
        
        disposables.add(
            stravaApiService.exchangeAuthorizationCode(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::handleTokenExchangeSuccess,
                    this::handleTokenExchangeError
                )
        );
    }
    
    private void handleTokenExchangeSuccess(TokenResponse tokenResponse) {
        Log.i(TAG, "Token exchange successful");
        
        // Save tokens
        tokenManager.saveToken(tokenResponse);
        
        // Show success and finish
        showSuccessState();
        
        Toast.makeText(this, "Strava account connected successfully!", Toast.LENGTH_SHORT).show();
    }
    
    private void handleTokenExchangeError(Throwable error) {
        Log.e(TAG, "Token exchange failed", error);
        
        String errorMessage = "Failed to complete authorization";
        if (error.getMessage() != null && !error.getMessage().isEmpty()) {
            errorMessage += ": " + error.getMessage();
        }
        
        showErrorState(errorMessage);
    }

    /**
     * Shows a dialog for manual authorization code entry.
     * This is useful when the automatic redirect doesn't work.
     */
    private void showManualCodeEntryDialog() {
        Log.i(TAG, "Showing manual authorization code entry dialog");
        
        // Create an EditText for code input
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter authorization code");
        
        // Create dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Manual Authorization")
            .setMessage("If you've completed authorization in a browser, paste the code from the redirect URL here:")
            .setView(input)
            .setPositiveButton("Submit", (dialog, which) -> {
                String code = input.getText().toString().trim();
                if (code.isEmpty()) {
                    Toast.makeText(this, "Code cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Log.i(TAG, "Manual authorization code entered, processing...");
                exchangeCodeForTokens(code);
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
            .show();
    }

    @Override
    public void onBackPressed() {
        // Handle back button press by setting cancelled result
        setResult(RESULT_CANCELLED);
        super.onBackPressed();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // If the activity is pausing and no result has been set yet, 
        // and we're not in processing state, consider it cancelled
        if (progressBar.getVisibility() != View.VISIBLE && !isFinishing()) {
            // User might be switching to browser - don't cancel yet
            Log.d(TAG, "Activity paused, possibly switching to browser");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
} 