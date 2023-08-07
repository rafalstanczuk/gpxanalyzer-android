package com.itservices.gpxanalyzer.logbook.chart.legend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.itservices.gpxanalyzer.R;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class PaletteColorDeterminer {

    private final Bitmap colorPalette;

    @Inject
    public PaletteColorDeterminer(@ApplicationContext Context context) {

        colorPalette = BitmapFactory.decodeResource(context.getResources(), R.drawable.color_pallette);

        //Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, false);

        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), colorPalette);

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

    public int determineDiscreteColorFromScaledValue(float value, float min, float max, int numOfDividing, PaletteDirection paletteDirection) {

        int maxYPalette = colorPalette.getHeight() - 1;
        int x = (int) Math.floor((colorPalette.getWidth() -1)/2.0);

        float stepYPalette =  maxYPalette / (float)numOfDividing ;

        float colorFloatScaledIndex = maxYPalette * ( (value - min)/(max - min) );

        int indexStep = (int) Math.floor( colorFloatScaledIndex / stepYPalette);

        int colorFloatScaledIndexSteppedDiscrete = (int) (indexStep * stepYPalette);

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

    public enum PaletteDirection {
        MIN_IS_ZERO_INDEX_Y_PIXEL,
        MAX_IS_ZERO_INDEX_Y_PIXEL
    }

}
