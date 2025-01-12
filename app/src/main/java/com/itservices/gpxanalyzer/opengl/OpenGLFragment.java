package com.itservices.gpxanalyzer.opengl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OpenGLFragment extends Fragment {

    private MyGLSurfaceView mGLSurfaceView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Create our custom OpenGL view
        mGLSurfaceView = new MyGLSurfaceView(requireContext());

        // Return it as the fragment's content
        return mGLSurfaceView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // GLSurfaceView must be paused when the fragment is paused.
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // GLSurfaceView must be resumed when the fragment is resumed.
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
    }
}
