package com.itservices.gpxanalyzer.core.ui.components.chart.settings.background;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.core.ui.components.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.core.ui.components.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.core.ui.components.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.core.utils.ui.ColorUtil;

import java.util.Map;

import javax.inject.Inject;

/**
 * Handles drawing colored background areas onto a {@link DataEntityLineChart}.
 * These background areas typically represent different zones or boundaries based on Y-axis values
 * (e.g., heart rate zones, speed zones).
 * It uses a {@link PaletteColorDeterminer} to define the color and vertical range (min/max Y-value)
 * for each background area.
 */
public class GridBackgroundDrawer {
    /** Alpha value applied to the background area colors for transparency. */
    private static final int BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA = 70;
    /** Default X-value used to determine the Y-pixel coordinates for the top and bottom of the background rectangles. */
    private static final float DEFAULT_START_DRAW_X = 0.0f;

    /**
     * Constructor used by Hilt for dependency injection.
     */
    @Inject
    public GridBackgroundDrawer() { }

    /**
     * Draws the grid background areas on the provided chart canvas.
     * Iterates through the color boundaries defined in the palette and calls
     * {@link #drawRectArea(Canvas, BoundaryColorSpan, DataEntityLineChart)} for each.
     *
     * @param lineChart The target chart.
     * @param paletteColorDeterminer The palette defining color boundaries.
     * @param canvas The canvas to draw on.
     */
    public void drawGridBackground(DataEntityLineChart lineChart, PaletteColorDeterminer paletteColorDeterminer, Canvas canvas) {
        for (Map.Entry<Integer, BoundaryColorSpan> entry : paletteColorDeterminer.getPalette().entrySet()) {
            drawRectArea(canvas, entry.getValue(), lineChart);
        }
    }

    /**
     * Draws a single rectangular background area for a given color boundary.
     * The rectangle spans the full width of the chart's content area and the vertical range
     * defined by the {@link BoundaryColorSpan}.
     *
     * @param canvas The canvas to draw on.
     * @param boundaryColorSpan The color boundary defining the color and vertical range.
     * @param lineChart The target chart (used to get view port and position information).
     */
    private void drawRectArea(Canvas canvas, BoundaryColorSpan boundaryColorSpan, DataEntityLineChart lineChart) {
        int colorForAreaId = ColorUtil.setAlphaInIntColor(boundaryColorSpan.color(), BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA);

        MPPointF leftMin = lineChart.getPosition(
                new Entry(DEFAULT_START_DRAW_X, boundaryColorSpan.min()), YAxis.AxisDependency.LEFT);
        MPPointF leftMax = lineChart.getPosition(
                new Entry(DEFAULT_START_DRAW_X, boundaryColorSpan.max()),
                YAxis.AxisDependency.LEFT
        );

        Paint paintMinMax = new Paint();
        paintMinMax.setColor(colorForAreaId);

        if (leftMax != null) {
            RectF rectMinMax = new RectF(lineChart.getViewPortHandler().contentLeft(), leftMax.y,
                    lineChart.getViewPortHandler().contentRight(), leftMin.y
            );

            canvas.drawRect(rectMinMax, paintMinMax);
        }
    }
}
