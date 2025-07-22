package com.itservices.gpxanalyzer.feature.gpxlist.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itservices.gpxanalyzer.R;

/**
 * Fallback WebView activity for handling Strava OAuth on devices without browsers.
 * This is especially useful for emulators that don't have Chrome or other browsers installed.
 * 
 * The activity loads the Strava authorization page in a WebView and captures the redirect
 * to extract the authorization code.
 */
public class StravaWebViewActivity extends AppCompatActivity {
    
    private static final String TAG = StravaWebViewActivity.class.getSimpleName();
    
    // Intent extras
    public static final String EXTRA_AUTH_URL = "auth_url";
    
    private WebView webView;
    private ProgressBar progressBar;
    private TextView statusText;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_oauth);
        
        initializeViews();
        
        String authUrl = getIntent().getStringExtra(EXTRA_AUTH_URL);
        if (authUrl == null || authUrl.isEmpty()) {
            showError("No authorization URL provided");
            return;
        }
        
        setupWebView(authUrl);
    }
    
    private void initializeViews() {
        webView = findViewById(R.id.webview_oauth);
        progressBar = findViewById(R.id.progress_webview);
        statusText = findViewById(R.id.tv_webview_status);
    }
    
    private void setupWebView(String authUrl) {
        Log.d(TAG, "Setting up WebView for URL: " + authUrl);
        
        // Configure WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // Set WebViewClient to handle page loading and URL interception
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();
                Log.d(TAG, "WebView loading URL: " + url);
                
                // Check if this is the redirect URL with the authorization code
                if (url.startsWith("http://localhost:8080/strava-auth")) {
                    Log.i(TAG, "Intercepted redirect URL: " + url);
                    handleAuthorizationResponse(uri);
                    return true;
                }
                
                // Let the WebView handle all other URLs
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                Log.d(TAG, "Page loading started: " + url);
                progressBar.setVisibility(View.VISIBLE);
                statusText.setText("Loading Strava authorization page...");
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page loading finished: " + url);
                progressBar.setVisibility(View.GONE);
                statusText.setText("Please authorize GPX Analyzer to access your Strava account");
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "WebView error: " + description + " (code: " + errorCode + ")");
                showError("Error loading page: " + description);
            }
        });
        
        // Load the authorization URL
        try {
            webView.loadUrl(authUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error loading authorization URL", e);
            showError("Error loading authorization page: " + e.getMessage());
        }
    }
    
    private void handleAuthorizationResponse(Uri responseUri) {
        Log.i(TAG, "Handling authorization response from WebView");
        
        // Extract authorization code or error
        String code = responseUri.getQueryParameter("code");
        String error = responseUri.getQueryParameter("error");
        
        if (error != null) {
            Log.e(TAG, "Authorization error: " + error);
            showError("Authorization failed: " + error);
            return;
        }
        
        if (code == null || code.isEmpty()) {
            Log.e(TAG, "No authorization code in response");
            showError("Invalid response from Strava");
            return;
        }
        
        Log.i(TAG, "Successfully obtained authorization code");
        Toast.makeText(this, "Authorization successful!", Toast.LENGTH_SHORT).show();
        
        // Forward the authorization response to StravaOAuthActivity
        Intent intent = new Intent(this, StravaOAuthActivity.class);
        intent.setData(responseUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        
        // Close this activity
        finish();
    }
    
    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("Error: " + message);
        Log.e(TAG, "Error: " + message);
        
        // Auto-finish after delay
        statusText.postDelayed(() -> finish(), 5000);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
} 