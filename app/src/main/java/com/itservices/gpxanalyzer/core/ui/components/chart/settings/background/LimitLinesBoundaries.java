package com.itservices.gpxanalyzer.core.ui.components.chart.settings.background;

import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.core.ui.components.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.core.ui.components.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.core.utils.ui.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

/**
 * Manages the creation, storage, and application of {@link LimitLine} objects for a chart's Y-axis.
 * These limit lines typically represent thresholds or boundaries derived from a color palette
 * (e.g., heart rate zones, speed zones), visually marking specific Y-values on the chart.
 * This class ensures thread-safe access and modification of the limit lines list.
 */
public class LimitLinesBoundaries {

    /** Default color for the limit line text labels. */
    private static final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

    /** Thread-safe storage for the list of managed limit lines. */
    private final AtomicReference<List<LimitLine>> limitLineList = new AtomicReference<>(new ArrayList<>());

    /**
     * Constructor used by Hilt for dependency injection.
     */
    @Inject
    public LimitLinesBoundaries() {
    }

    /**
     * Creates and configures a single {@link LimitLine}.
     * Sets the limit value, label, position, color, line style (dashed), and text size.
     *
     * @param limitValue The Y-axis value where the line should be drawn.
     * @param labelPosition The position of the label relative to the line.
     * @return A configured {@link LimitLine} object.
     */
    private static LimitLine createLimitLine(float limitValue, LimitLine.LimitLabelPosition labelPosition) {
        LimitLine line = new LimitLine(limitValue, String.valueOf((int) limitValue));
        line.setLabelPosition(labelPosition);
        line.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
        line.setLineWidth(0.4f);
        line.setTextSize(10f);
        line.enableDashedLine(10f, 5f, 0f);
        line.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
        return line;
    }

    /**
     * Initializes or re-initializes the list of limit lines based on a provided color palette.
     * Clears any existing lines and creates new ones corresponding to the maximum value of each
     * {@link BoundaryColorSpan} in the palette. An additional line might be added above the highest boundary.
     * This operation is synchronized to ensure thread safety.
     *
     * @param paletteColorDeterminer The palette provider used to get boundary information.
     */
    public synchronized void initLimitLines(PaletteColorDeterminer paletteColorDeterminer) {
        Map<Integer, BoundaryColorSpan> paletteMap = paletteColorDeterminer.getPalette();

        synchronized (limitLineList) {
            List<LimitLine> limitLines = limitLineList.get();
            limitLines.clear();

            for (Map.Entry<Integer, BoundaryColorSpan> entry : paletteMap.entrySet()) {
                limitLines.add(
                        createLimitLine(entry.getValue().max(), LimitLine.LimitLabelPosition.LEFT_BOTTOM)
                );
            }

            BoundaryColorSpan lastBoundaryColorSpan = paletteMap.get(paletteMap.size() - 1);
            BoundaryColorSpan beforeLastBoundaryColorSpan = paletteMap.get(paletteMap.size() - 2);

            if (lastBoundaryColorSpan != null) {
                assert beforeLastBoundaryColorSpan != null;
                limitLines.add(
                        createLimitLine(
                                (lastBoundaryColorSpan.max() + (lastBoundaryColorSpan.max() - beforeLastBoundaryColorSpan.max())),
                                LimitLine.LimitLabelPosition.LEFT_BOTTOM)
                );
            }
        }
    }

    /**
     * Returns the current list of managed {@link LimitLine} objects.
     * This operation is synchronized.
     *
     * @return A list of {@link LimitLine} objects.
     */
    public synchronized List<LimitLine> getLimitLineList() {
        return limitLineList.get();
    }

    /**
     * Adds all managed limit lines to the specified {@link YAxis}.
     * This operation is synchronized.
     *
     * @param yAxisLeft The Y-axis to which the limit lines should be added.
     */
    public synchronized void addLimitLinesInto(YAxis yAxisLeft) {
        for (LimitLine limitLine : limitLineList.get()) {
            if (limitLine != null) {
                yAxisLeft.addLimitLine(limitLine);
            }
        }
    }
}
