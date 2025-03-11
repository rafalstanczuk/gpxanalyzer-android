package com.itservices.gpxanalyzer.chart.settings.background;

import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class LimitLinesBoundaries {

    private static final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

    private final List<LimitLine> limitLineList = new ArrayList<>();

    @Inject
    public LimitLinesBoundaries() { }

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

    public void initLimitLines(PaletteColorDeterminer paletteColorDeterminer) {
        Map<Integer, BoundaryColorSpan> paletteMap = paletteColorDeterminer.getPalette();
        limitLineList.clear();

        for (Map.Entry<Integer, BoundaryColorSpan> entry : paletteMap.entrySet()) {
            limitLineList.add(
                    createLimitLine(entry.getValue().max(), LimitLine.LimitLabelPosition.LEFT_BOTTOM)
            );
        }

        BoundaryColorSpan lastBoundaryColorSpan = paletteMap.get(paletteMap.size() - 1);
        BoundaryColorSpan beforeLastBoundaryColorSpan = paletteMap.get(paletteMap.size() - 2);

        if (lastBoundaryColorSpan != null) {
            assert beforeLastBoundaryColorSpan != null;
            limitLineList.add(
                    createLimitLine(
                            (lastBoundaryColorSpan.max() + (lastBoundaryColorSpan.max() - beforeLastBoundaryColorSpan.max()) ),
                            LimitLine.LimitLabelPosition.LEFT_BOTTOM)
            );
        }
    }

    public List<LimitLine> getLimitLineList() {
        return limitLineList;
    }

    public void addLimitLinesInto(YAxis yAxisLeft) {
        for (LimitLine limitLine : limitLineList) {
            if (limitLine != null) {
                yAxisLeft.addLimitLine(limitLine);
            }
        }
    }
}
