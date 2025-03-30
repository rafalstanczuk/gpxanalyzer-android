package com.itservices.gpxanalyzer.chart.settings.background;

import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.chart.palette.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

public class LimitLinesBoundaries {

    private static final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

    private final AtomicReference<List<LimitLine>> limitLineList = new AtomicReference<>(new ArrayList<>());

    @Inject
    public LimitLinesBoundaries() {
    }

    private static LimitLine createLimitLine(float limitValue, LimitLine.LimitLabelPosition labelPosition) {
        LimitLine line = new LimitLine(limitValue, String.valueOf((int) limitValue));
        line.setLabelPosition(labelPosition);
        line.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
        line.setLineWidth(0.4f);
        line.setTextSize(6f);
        line.enableDashedLine(10f, 5f, 0f);
        line.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
        return line;
    }

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

    public synchronized List<LimitLine> getLimitLineList() {
        return limitLineList.get();
    }

    public synchronized void addLimitLinesInto(YAxis yAxisLeft) {
        for (LimitLine limitLine : limitLineList.get()) {
            if (limitLine != null) {
                yAxisLeft.addLimitLine(limitLine);
            }
        }
    }
}
