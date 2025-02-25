package com.itservices.gpxanalyzer.chart.settings.background;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.MPPointF;
import com.itservices.gpxanalyzer.chart.DataEntityLineChart;
import com.itservices.gpxanalyzer.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import java.util.Map;

import javax.inject.Inject;

public class GridBackgroundDrawer {
    private static final int BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA = 70;
    private static final float DEFAULT_START_DRAW_X = 0.0f;

    @Inject
    public GridBackgroundDrawer() { }

    public void drawGridBackground(DataEntityLineChart lineChart, PaletteColorDeterminer paletteColorDeterminer, Canvas canvas) {
        for (Map.Entry<Integer, BoundaryColorSpan> entry : paletteColorDeterminer.getPalette().entrySet()) {
            drawRectArea(canvas, entry.getValue(), lineChart);
        }
    }

    private void drawRectArea(Canvas canvas, BoundaryColorSpan boundaryColorSpan, DataEntityLineChart lineChart) {
        int colorForAreaId = ColorUtil.setAlphaInIntColor(boundaryColorSpan.getColor(), BACKGROUND_BOUNDARIES_AREA_COLOR_ALPHA);

        MPPointF leftMin = lineChart.getPosition(
                new Entry(DEFAULT_START_DRAW_X, boundaryColorSpan.getMin()), YAxis.AxisDependency.LEFT);
        MPPointF leftMax = lineChart.getPosition(
                new Entry(DEFAULT_START_DRAW_X, boundaryColorSpan.getMax()),
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
