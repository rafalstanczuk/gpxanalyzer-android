package com.itservices.gpxanalyzer;

import androidx.multidex.MultiDexApplication;
import dagger.hilt.android.HiltAndroidApp;

/**
 * Main application class for the GPX Analyzer application.
 * This class extends MultiDexApplication to support a large number of methods
 * and is annotated with HiltAndroidApp to enable dependency injection.
 *
 * The application class serves as the entry point for the app and initializes
 * the dependency injection framework.
 */
@HiltAndroidApp
public class App extends MultiDexApplication {
	/**
	 * Called when the application is being terminated.
	 * This is the last callback that the application will receive.
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	/**
	 * Called when the application is starting, before any activity, service,
	 * or receiver objects (excluding content providers) have been created.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}
}