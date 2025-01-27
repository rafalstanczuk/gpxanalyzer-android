package com.itservices.gpxanalyzer.spectrum.androidview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

public class SpectrumView extends View {
    private Paint paint = new Paint();
    private double[] spectrum;

    public SpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLUE);
    }

    public void updateSpectrum(double[] spectrum) {
        this.spectrum = Arrays.copyOf(spectrum, spectrum.length);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (spectrum != null) {
            float width = getWidth();
            float height = getHeight();
            float barWidth = width / spectrum.length;
            for (int i = 0; i < spectrum.length; i++) {
                float barHeight = (float) (spectrum[i] / 32768.0 * height);
                canvas.drawRect(i * barWidth, height - barHeight, (i + 1) * barWidth, height, paint);
            }
        }
    }
}