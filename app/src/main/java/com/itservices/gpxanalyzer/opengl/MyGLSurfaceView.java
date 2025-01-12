package com.itservices.gpxanalyzer.opengl;


import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Use OpenGL ES 2.0
        setEGLContextClientVersion(2);

        // Create an instance of our custom renderer
        mRenderer = new MyRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        // (If you want continuous rendering, use RENDERMODE_CONTINUOUSLY)
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}