package com.itservices.gpxanalyzer.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Called once when the surface is created or recreated.
        // Set a background color, for example.
        GLES20.glClearColor(0.0f, 0.5f, 0.0f, 1.0f);  // Greenish background
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Called if the geometry of the view changes (e.g. screen rotation)
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Called to draw the current frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Here you would add your rendering logic.
    }
}