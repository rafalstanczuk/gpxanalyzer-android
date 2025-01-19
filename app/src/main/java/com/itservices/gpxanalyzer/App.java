package com.itservices.gpxanalyzer;

import androidx.multidex.MultiDexApplication;


import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends MultiDexApplication {
	@Override
	public void onTerminate() {

		super.onTerminate();
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}
}