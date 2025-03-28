package com.itservices.gpxanalyzer.ui.mapview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MapViewFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {

        return getView();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


}