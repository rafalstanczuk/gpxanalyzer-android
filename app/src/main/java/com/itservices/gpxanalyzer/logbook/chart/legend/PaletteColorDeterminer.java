package com.itservices.gpxanalyzer.logbook.chart.legend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.logbook.chart.data.StatisticResults;
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
    private final Bitmap colorPalette;

    private Map<Integer, BoundaryColorSpan> paletteMap = new HashMap<>();
    private StatisticResults statisticResults;

    @Inject
    public PaletteColorDeterminer(@ApplicationContext Context context) {

        colorPalette = BitmapFactory.decodeResource(context.getResources(), R.drawable.color_pallette);
        paletteNumberOfDivisions = context.getResources().getInteger(R.integer.palette_number_of_divisions);
        //Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, false);

        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), colorPalette);

    }

    public void initPalette(StatisticResults statisticResults) {
        this.statisticResults = statisticResults;
        paletteMap = generatePalette(
                (float) statisticResults.getMinValue(),
                (float) statisticResults.getMaxValue(),
                paletteNumberOfDivisions,
                PaletteDirection.MAX_IS_ZERO_INDEX_Y_PIXEL);
    }

    public BoundaryColorSpan getBoundaryFrom(float valToTest) {

        float normalizedVal = valToTest - (float)statisticResults.getMinValue();

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

    private LinkedHashMap<Integer, BoundaryColorSpan> generatePalette(float min, float max, int numOfDividing, PaletteDirection paletteDirection) {
        LinkedHashMap<Integer, BoundaryColorSpan> boundaryColorSpan = new LinkedHashMap<>();

        int maxYPalette = colorPalette.getHeight() - 1;
        int x = (int) Math.floor((colorPalette.getWidth() -1)/2.0);

        float stepYPalette =  maxYPalette / (float)numOfDividing ;
        float valuesSpann = max - min;
        float stepValue =  valuesSpann / (float)numOfDividing ;

        float shiftFromStartValueOfBoundary = 0.5f * stepValue;

        for( int i=0; i < numOfDividing; i++) {
            float value = i * stepValue + min;
            float nextValue = value + stepValue;

            float middleValueOfCurrentBoundary = value + shiftFromStartValueOfBoundary;
            float colorFloatScaledIndex = maxYPalette * ( (middleValueOfCurrentBoundary - min)/ valuesSpann );

            int indexColorStep = (int) Math.floor( colorFloatScaledIndex / stepYPalette);

            int colorFloatScaledIndexSteppedDiscrete = (int) (indexColorStep * stepYPalette);

            int scaledPixelColor = determineDiscreteColorFromScaledValue(x, maxYPalette, colorFloatScaledIndexSteppedDiscrete, paletteDirection);

            boundaryColorSpan.put(i, new BoundaryColorSpan(i, String.valueOf(value), value, nextValue, scaledPixelColor));
        }

        return boundaryColorSpan;
    }

    private int determineDiscreteColorFromScaledValue(int x, int maxYPalette, int colorFloatScaledIndexSteppedDiscrete, PaletteDirection paletteDirection) {
        int scaledPixelColor = colorPalette.getPixel(x,  colorFloatScaledIndexSteppedDiscrete);

        switch (paletteDirection) {

            case MIN_IS_ZERO_INDEX_Y_PIXEL:
                scaledPixelColor = colorPalette.getPixel(x, colorFloatScaledIndexSteppedDiscrete);
                break;
            case MAX_IS_ZERO_INDEX_Y_PIXEL:
                scaledPixelColor = colorPalette.getPixel(x, maxYPalette - colorFloatScaledIndexSteppedDiscrete);
                break;
        }

        return scaledPixelColor;
    }
}
