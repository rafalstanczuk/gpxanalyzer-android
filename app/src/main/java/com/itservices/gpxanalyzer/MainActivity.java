package com.itservices.gpxanalyzer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavInflater navInflater = navController.getNavInflater();
        NavGraph graph = navInflater.inflate(R.navigation.nav_graph);

        graph.setStartDestination(R.id.logbookFragment);

        navController.setGraph(graph);

        //bottomNav = findViewById(R.id.bottom_navigation);
        //setBottomNavigationMenu(bottomNav);
        //NavigationUI.setupWithNavController(bottomNav, navController);
        ///////////////////////////////

        // listen to destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

        });




    }

    private void setBottomNavigationMenu(@NonNull BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().clear();
/*
        String settingsLanguage = Settings.sharedInstance(getApplicationContext()).getLanguage();

        if (settingsLanguage.equals("de")) {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_de);
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu_en);
        }*/
    }

}