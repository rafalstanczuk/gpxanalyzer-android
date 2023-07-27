package com.itservices.gpxanalyzer;

import static com.itservices.gpxanalyzer.logbook.chart.entry.IconsUtil.getTimeAsIntFromDate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itservices.gpxanalyzer.logbook.Measurement;
import com.itservices.gpxanalyzer.logbook.StatisticResults;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Extensions;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

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