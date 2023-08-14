package com.itservices.gpxanalyzer.logbook.chart.legend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

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

    private final Bitmap colorPalette;

    private Map<Integer, BoundaryColorSpan> paletteMap = new HashMap<>();

    @Inject
    public PaletteColorDeterminer(@ApplicationContext Context context) {

        colorPalette = BitmapFactory.decodeResource(context.getResources(), R.drawable.color_pallette);

        //Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, false);

        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), colorPalette);

    }

    public void initPalette(StatisticResults statisticResults) {
        LinkedHashMap<Integer, BoundaryColorSpan> paletteMap = new LinkedHashMap<>();

        paletteMap = generatePalette(
                (float) 0,
                (float) statisticResults.getMaxValue(),
                10,
                PaletteColorDeterminer.PaletteDirection.MAX_IS_ZERO_INDEX_Y_PIXEL);

        float valToTest = 120.0f;

        BoundaryColorSpan boundaryColorSpan = getBoundaryFrom(valToTest, paletteMap);
    }

    private BoundaryColorSpan getBoundaryFrom(float valToTest, LinkedHashMap<Integer, BoundaryColorSpan> paletteMap) {

        BoundaryColorSpan first =  paletteMap.entrySet().iterator().next().getValue();

        float delta = first.getMax() - first.getMin();

        int estimatedKeyIndex = (int) Math.floor( valToTest / delta );

        BoundaryColorSpan objectFound = paletteMap.get(estimatedKeyIndex);

        return objectFound;
    }

    public int determineColorFromScaledValue(float value, float min, float max, PaletteDirection paletteDirection) {

        int maxYPalette = colorPalette.getHeight() - 1;
        int x = (int) Math.floor((colorPalette.getWidth() -1)/2.0);

        float colorFloatScaledIndex = maxYPalette * ( (value - min)/(max - min) );

        int scaledPixelColor = colorPalette.getPixel(x, (int) colorFloatScaledIndex);

        switch (paletteDirection) {

            case MIN_IS_ZERO_INDEX_Y_PIXEL:
                scaledPixelColor = colorPalette.getPixel(x, (int) colorFloatScaledIndex);
                break;
            case MAX_IS_ZERO_INDEX_Y_PIXEL:
                scaledPixelColor = colorPalette.getPixel(x, maxYPalette - (int) colorFloatScaledIndex);
                break;
        }

        return scaledPixelColor;
    }

    public static boolean isWithinBoundary(float value, BoundaryColorSpan boundaryColorSpan) {
        return PrecisionUtil.isGreaterEqual(boundaryColorSpan.getMin(), value, PrecisionUtil.NDIG_PREC_COMP)
                &&
                value < boundaryColorSpan.getMax();
    }

    public LinkedHashMap<Integer, BoundaryColorSpan> generatePalette(float min, float max, int numOfDividing, PaletteDirection paletteDirection) {
        LinkedHashMap<Integer, BoundaryColorSpan> boundaryColorSpan = new LinkedHashMap<>();

        int maxYPalette = colorPalette.getHeight() - 1;
        int x = (int) Math.floor((colorPalette.getWidth() -1)/2.0);

        float stepYPalette =  maxYPalette / (float)numOfDividing ;
        float stepValue =  (max - min) / (float)numOfDividing ;

        for( int i=0; i < numOfDividing; i++) {
            float value = i * stepValue + min;
            float nextValue = (i+1) * stepValue + min;
            float colorFloatScaledIndex = maxYPalette * ((value - min) / (max - min));

            int indexStep = (int) Math.ceil( colorFloatScaledIndex / stepYPalette);

            int colorFloatScaledIndexSteppedDiscrete = (int) (indexStep * stepYPalette);

            int scaledPixelColor = colorPalette.getPixel(x, colorFloatScaledIndexSteppedDiscrete);

            switch (paletteDirection) {

                case MIN_IS_ZERO_INDEX_Y_PIXEL:
                    scaledPixelColor = colorPalette.getPixel(x, colorFloatScaledIndexSteppedDiscrete);
                    break;
                case MAX_IS_ZERO_INDEX_Y_PIXEL:
                    scaledPixelColor = colorPalette.getPixel(x, maxYPalette - colorFloatScaledIndexSteppedDiscrete);
                    break;
            }

            boundaryColorSpan.put(i, new BoundaryColorSpan(i, String.valueOf(value), value, nextValue, scaledPixelColor));
        }

        return boundaryColorSpan;
    }

    public int determineDiscreteColorFromScaledValue(float value, float min, float max, int numOfDividing, PaletteDirection paletteDirection) {

        int maxYPalette = colorPalette.getHeight() - 1;
        int x = (int) Math.floor((colorPalette.getWidth() -1)/2.0);

        float stepYPalette =  maxYPalette / (float)numOfDividing ;

        float colorFloatScaledIndex = maxYPalette * ( (value - min)/(max - min) );

        int indexStep = (int) Math.ceil( colorFloatScaledIndex / stepYPalette);

        int colorFloatScaledIndexSteppedDiscrete = (int) (indexStep * stepYPalette);

        int scaledPixelColor = colorPalette.getPixel(x,  colorFloatScaledIndexSteppedDiscrete);

/*        Log.d("PaletteColorDeterminer", "determineDiscreteColorFromScaledValue() called with: value = [" + value + "], min = [" + min + "], max = [" + max + "], numOfDividing = [" + numOfDividing + "], paletteDirection = [" + paletteDirection + "]");

        Log.d("PaletteColorDeterminer",
                String.format(
                        "indexStep=%d\n, " +
                        "colorFloatScaled=%d\n, " +
                        "stepYPalette=%f\n, " +
                        "maxYPalette=%d\n",
                        indexStep, colorFloatScaledIndexSteppedDiscrete, stepYPalette, maxYPalette ));*/

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

    public enum PaletteDirection {
        MIN_IS_ZERO_INDEX_Y_PIXEL,
        MAX_IS_ZERO_INDEX_Y_PIXEL
    }

}
