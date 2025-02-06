package com.itservices.gpxanalyzer.ui.spectrum.opengl;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class SpectrumGLSurfaceView extends GLSurfaceView {

    private SpectrumRenderer renderer;

    public SpectrumGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public SpectrumGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Use OpenGL ES 2.0
        setEGLContextClientVersion(2);

        // Create renderer
        renderer = new SpectrumRenderer();
        setRenderer(renderer);

        // If you want to update only on data changes:
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public SpectrumRenderer getRenderer() {
        return renderer;
    }
}