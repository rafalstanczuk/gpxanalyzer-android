package com.itservices.gpxanalyzer.logbook.chart.legend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.StatisticResults;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class PaletteColorDeterminer {

    private final int paletteNumberOfDivisions;
    private final int[] paletteFromBitmap;
    private Map<Integer, BoundaryColorSpan> paletteMap = new HashMap<>();

    protected StatisticResults statisticResults;

    @Inject
    public PaletteColorDeterminer(@ApplicationContext Context context) {
        Bitmap colorPalette = BitmapFactory.decodeResource(context.getResources(), R.drawable.color_palette);
        paletteNumberOfDivisions = context.getResources().getInteger(R.integer.palette_number_of_divisions);
        paletteFromBitmap = compactPalettePixelsFrom(colorPalette);
    }

    @NonNull
    private static int[] compactPalettePixelsFrom(Bitmap colorPalette) {
        final int[] paletteFromBitmap;
        paletteFromBitmap = new int[colorPalette.getHeight()];
        for(int i = 0; i < colorPalette.getHeight(); i++) {
            paletteFromBitmap[i] = colorPalette.getPixel(1, i);
        }
        return paletteFromBitmap;
    }

    public void initPalette(StatisticResults statisticResults) {
        this.statisticResults = statisticResults;

        paletteMap = generatePalette(
                (float) statisticResults.getMinValue(),
                (float) statisticResults.getMaxValue(),
                paletteNumberOfDivisions,
                paletteFromBitmap,
                PaletteDirection.MAX_IS_ZERO_INDEX_Y_PIXEL);
    }

    public Map<Integer, BoundaryColorSpan> getPalette() {
        return paletteMap;
    }

    public BoundaryColorSpan getBoundaryFrom(float value) {

        float normalizedVal = value - (float)statisticResults.getMinValue();

        BoundaryColorSpan first =  paletteMap.entrySet().iterator().next().getValue();

        float delta = first.getMax() - first.getMin();

        int estimatedKeyIndex = (int) Math.floor( normalizedVal / delta );

        int estimatedKeyIndexLowIndexCheck = Math.max(estimatedKeyIndex, 0);

        int estimatedKeyIndexHighestIndexCheck = Math.min(estimatedKeyIndexLowIndexCheck, paletteMap.size() - 1);

        int finalEstimatedKeyIndex = estimatedKeyIndexHighestIndexCheck;

        BoundaryColorSpan objectFound = paletteMap.get(finalEstimatedKeyIndex);

        return objectFound;
    }

    public static boolean isWithinBoundary(float value, BoundaryColorSpan boundaryColorSpan) {
        return PrecisionUtil.isGreaterEqual(boundaryColorSpan.getMin(), value, PrecisionUtil.NDIG_PREC_COMP)
                &&
                value < boundaryColorSpan.getMax();
    }

    private LinkedHashMap<Integer, BoundaryColorSpan> generatePalette(float min, float max, int numOfDividing, int[] paletteFromBitmap, PaletteDirection paletteDirection) {
        LinkedHashMap<Integer, BoundaryColorSpan> boundaryColorSpan = new LinkedHashMap<>();

        int maxYPalette = paletteFromBitmap.length - 1;

        float stepYPalette =  maxYPalette / (float)numOfDividing ;
        float valuesSpan = max - min;
        float stepValue =  valuesSpan / (float)numOfDividing ;

        float shiftFromStartValueOfBoundary = 0.5f * stepValue;

        for( int i=0; i < numOfDividing; i++) {
            float value = i * stepValue + min;
            float nextValue = value + stepValue;

            float middleValueOfCurrentBoundary = value + shiftFromStartValueOfBoundary;
            float colorFloatScaledIndex = maxYPalette * ( (middleValueOfCurrentBoundary - min)/ valuesSpan );

            int indexColorStep = (int) Math.floor( colorFloatScaledIndex / stepYPalette);

            int colorFloatScaledIndexSteppedDiscrete = (int) (indexColorStep * stepYPalette);

            int scaledPixelColor = determineDiscreteColorFromScaledValue(maxYPalette, colorFloatScaledIndexSteppedDiscrete, paletteDirection);

            boundaryColorSpan.put(i, new BoundaryColorSpan(i, String.valueOf(value), value, nextValue, scaledPixelColor));
        }

        return boundaryColorSpan;
    }

    private int determineDiscreteColorFromScaledValue(int maxYPalette, int colorFloatScaledIndexSteppedDiscrete, PaletteDirection paletteDirection) {
        int scaledPixelColor = paletteFromBitmap[colorFloatScaledIndexSteppedDiscrete];

        switch (paletteDirection) {

            case MIN_IS_ZERO_INDEX_Y_PIXEL:
                scaledPixelColor = paletteFromBitmap[colorFloatScaledIndexSteppedDiscrete];
                break;
            case MAX_IS_ZERO_INDEX_Y_PIXEL:
                scaledPixelColor = paletteFromBitmap[maxYPalette - colorFloatScaledIndexSteppedDiscrete];
                break;
        }

        return scaledPixelColor;
    }
}
