package com.itservices.gpxanalyzer.spectrum.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.util.Pair;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Minimal example of drawing the audioSpectrum in OpenGL ES 2.0
 */
public class SpectrumRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "SpectrumRenderer";

    // Simple shader source (for drawing lines or points)
    private static final String VERTEX_SHADER_CODE =
            "attribute vec4 aPosition;   \n" +
                    "void main() {               \n" +
                    "    gl_Position = aPosition;\n" +
                    "}";

    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;  \n" +
                    "uniform vec4 uColor;      \n" +
                    "void main() {             \n" +
                    "    gl_FragColor = uColor;\n" +
                    "}";

    // We’ll store our (x, y) coords in a FloatBuffer
    private FloatBuffer vertexBuffer;
    private int program;
    private int positionHandle;
    private int colorHandle;

    // Let’s keep track of how many points we want to draw
    private int numberOfPoints = 0;

    // We can define a color for the spectrum line or points
    private static final float[] DRAW_COLOR = {1.0f, 0f, 0f, 1.0f}; // Red RGBA

    // Screen coordinate reference
    // We'll map frequency on the X-axis: [0..maxFreq] => [-1..+1]
    // We'll map amplitude on the Y-axis: [0..maxAmp]  => [-1..+1]
    // or some smaller range. This is up to you to decide scaling.


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set clear color (background)
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        // Compile the shaders, link program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        // Get attribute/uniform handles
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        colorHandle = GLES20.glGetUniformLocation(program, "uColor");
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl,
                                 int width, int height) {
        // Adjust viewport if needed
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
        // Clear the screen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Use our custom program
        GLES20.glUseProgram(program);

        // Pass color
        GLES20.glUniform4fv(colorHandle, 1, DRAW_COLOR, 0);

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the vertex coordinate data
        if (vertexBuffer != null) {
            vertexBuffer.position(0);
            GLES20.glVertexAttribPointer(
                    positionHandle,
                    2,               // (x, y)
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    vertexBuffer
            );

            // Draw the points as a line strip or as points
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, numberOfPoints);
            // If you want discrete points, do: GLES20.GL_POINTS
        }

        // Disable the attribute
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    /**
     * Update the list of (freq, amplitude) pairs, build a FloatBuffer for drawing.
     */
    public void setSpectrumData(List<Pair<Float, Double>> spectrumData) {
        if (spectrumData == null || spectrumData.isEmpty()) {
            numberOfPoints = 0;
            vertexBuffer = null;
            return;
        }

        numberOfPoints = spectrumData.size();

        // Create an array of float coordinates: [x0, y0, x1, y1, ..., xN, yN]
        float[] coords = new float[numberOfPoints * 2];
        float largestFreq = 0f;
        double largestAmp = 0.0;

        // First, find max freq & amplitude if you want auto-scaling
        // (Optional - you can also fix these if you know your range)
        for (Pair<Float, Double> pair : spectrumData) {
            if (pair.first > largestFreq) largestFreq = pair.first;
            if (pair.second > largestAmp) largestAmp = pair.second;
        }
        // If you already have known "maxFrequency"/"maxAmplitude" from your sampleRate, you can use them.
        // For example, if sampleRate = 44100, maxFrequency ~ 22050, etc.
        // Or you can just do dynamic scaling as below:
        float freqScale = (largestFreq == 0) ? 1f : largestFreq;
        double ampScale = (largestAmp == 0.0) ? 1.0 : largestAmp;

        // Fill coords array
        for (int i = 0; i < numberOfPoints; i++) {
            float freq = spectrumData.get(i).first;   // X
            double amp = spectrumData.get(i).second;  // Y

            // Normalize to [-1..1], or [0..1], depending on preference
            // For example, let’s do [0..1] in X and Y (shift up to -1..+1 if you prefer)
            float x = (freq / freqScale) * 2f - 1f;     // scale to [0..1], then shift to [-1..0]
            float y = (float) ((amp / ampScale) * 2f - 1f);

            coords[(i * 2)]     = x;
            coords[(i * 2) + 1] = y;
        }

        // Now create the vertex buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4); // 4 bytes per float
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);
    }

    /**
     * Utility method to compile a shader
     */
    private static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}

