package com.itservices.gpxanalyzer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

import com.itservices.gpxanalyzer.usecase.SelectGpxFileUseCase;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Main activity of the GPX Analyzer application.
 * This activity serves as the host for the navigation graph and handles
 * the initial setup of the application's navigation structure.
 *
 * The activity is annotated with AndroidEntryPoint to enable dependency injection
 * and uses the Navigation component for handling navigation between different
 * fragments of the application.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    SelectGpxFileUseCase selectGpxFileUseCase;

    /**
     * Called when the activity is first created.
     * This method initializes the navigation structure and sets up the file
     * selection functionality.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the data it most recently supplied in onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the file selection launcher
        selectGpxFileUseCase.registerLauncherOn(this);

        // Initialize navigation components
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        // Set up the navigation graph
        NavInflater navInflater = navController.getNavInflater();
        NavGraph graph = navInflater.inflate(R.navigation.nav_graph);
/*
        graph.setStartDestination(R.id.mainMenuFragment);*/

        navController.setGraph(graph);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

        });
    }
}