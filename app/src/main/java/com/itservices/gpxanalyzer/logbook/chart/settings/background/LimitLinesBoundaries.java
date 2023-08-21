package com.itservices.gpxanalyzer.logbook.chart.settings.background;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.itservices.gpxanalyzer.logbook.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.logbook.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.utils.ui.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class LimitLinesBoundaries {

	@Inject
	PaletteColorDeterminer palette;

	private List<LimitLine> limitLineList = new ArrayList<>();

	private static final int DEFAULT_LIMIT_LINES_COLOR = Color.BLACK;

	@Inject
	public LimitLinesBoundaries(@ApplicationContext Context context) {

	}

	public void initLimitLines() {
		Map<Integer, BoundaryColorSpan> paletteMap = palette.getPalette();

		for(Map.Entry<Integer, BoundaryColorSpan> entry: paletteMap.entrySet()) {
			limitLineList.add(
				createLimitLine((int)entry.getValue().getMax(), LimitLine.LimitLabelPosition.LEFT_BOTTOM)
			);
		}

		BoundaryColorSpan lastBoundaryColorSpan = paletteMap.get(paletteMap.size()-1);

		if (lastBoundaryColorSpan != null) {
			limitLineList.add(
					createLimitLine((int) (lastBoundaryColorSpan.getMax() * 1.2f), LimitLine.LimitLabelPosition.LEFT_BOTTOM)
			);
		}
	}

	private static LimitLine createLimitLine(float limitValue, LimitLine.LimitLabelPosition labelPosition) {
		LimitLine line = new LimitLine(limitValue, String.valueOf((int)limitValue));
			line.setLabelPosition(labelPosition);
			line.setTextColor(DEFAULT_LIMIT_LINES_COLOR);
			line.setLineWidth(0.4f);
			line.setTextSize(6f);
			line.enableDashedLine(10f, 5f, 0f);
			line.setLineColor(ColorUtil.setAlphaInIntColor(Color.GRAY, 128));
		return line;
	}

	public List<LimitLine> getLimitLineList() {
		return limitLineList;
	}

	public void addLimitLinesInto(YAxis yAxisLeft) {
		for(LimitLine limitLine: limitLineList) {
			if (limitLine != null) {
				yAxisLeft.addLimitLine(limitLine);
			}
		}
	}
}
