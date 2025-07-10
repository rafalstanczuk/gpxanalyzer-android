package com.itservices.gpxanalyzer.ui.components.chart.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.ui.components.chart.legend.BoundaryColorSpan;
import com.itservices.gpxanalyzer.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.utils.common.PrecisionUtil;
import com.itservices.gpxanalyzer.utils.ui.IconsUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Provides color mapping functionality for chart visualizations based on data values.
 * <p>
 * This class loads a color palette resource and maps data values to specific colors
 * within that palette. It divides the range of possible data values into discrete 
 * intervals (boundaries) and associates each interval with a specific color and icon.
 * <p>
 * The color mapping is particularly useful for visualizing data trends, where different
 * ranges of values can be represented by different colors (e.g., speed zones, elevation
 * changes, or other numerical properties).
 */
public class PaletteColorDeterminer {

    /** Number of discrete color divisions in the palette. */
    private final int paletteNumberOfDivisions;
    
    /** Array of colors extracted from the palette bitmap. */
    private final int[] paletteFromBitmap;
    
    /** Map of boundary index to color boundary definition. */
    private Map<Integer, BoundaryColorSpan> paletteMap = new HashMap<>();
    
    /** Map of boundary index to drawable icons representing each color. */
    private Map<Integer, Drawable> drawableMap = new LinkedHashMap<>();

    /** The data wrapper providing context for value ranges. */
    protected DataEntityWrapper dataEntityWrapper;

    /**
     * Creates a new PaletteColorDeterminer instance.
     * <p>
     * Loads the color palette bitmap and extracts the color array.
     *
     * @param context Application context for accessing resources
     */
    @Inject
    public PaletteColorDeterminer(@ApplicationContext Context context) {
        Bitmap colorPalette = BitmapFactory.decodeResource(context.getResources(), R.drawable.color_palette);
        paletteNumberOfDivisions = context.getResources().getInteger(R.integer.palette_number_of_divisions);
        paletteFromBitmap = compactPalettePixelsFrom(colorPalette);
    }

    /**
     * Extracts colors from the palette bitmap into a compact array.
     * <p>
     * The method reads one pixel from each row of the bitmap to create
     * a color array representing the vertical gradient of the palette.
     *
     * @param colorPalette The bitmap containing the color palette
     * @return An array of integer color values
     */
    @NonNull
    private static int[] compactPalettePixelsFrom(Bitmap colorPalette) {
        final int[] paletteFromBitmap;
        paletteFromBitmap = new int[colorPalette.getHeight()];
        for (int i = 0; i < colorPalette.getHeight(); i++) {
            paletteFromBitmap[i] = colorPalette.getPixel(1, i);
        }
        return paletteFromBitmap;
    }

    /**
     * Sets the data wrapper and initializes the color mappings.
     * <p>
     * This method recalculates the palette boundaries based on the min and max
     * values from the provided data wrapper, and regenerates the drawable icons.
     *
     * @param dataEntityWrapper The data wrapper containing value range information
     */
    public void setDataEntityWrapper(DataEntityWrapper dataEntityWrapper) {
        this.dataEntityWrapper = dataEntityWrapper;

        paletteMap = generatePalette(
                (float) dataEntityWrapper.getMinValue(),
                (float) dataEntityWrapper.getMaxValue(),
                paletteNumberOfDivisions,
                paletteFromBitmap,
                PaletteDirection.MAX_IS_ZERO_INDEX_Y_PIXEL);

        drawableMap = generateDrawableIconMap(paletteMap);
    }

    /**
     * Generates drawable icons for each color boundary.
     * <p>
     * Creates a map of icons where each icon uses the color from the
     * corresponding boundary in the palette.
     *
     * @param paletteMap The map of color boundaries
     * @return A map of drawable icons for each boundary
     */
    private Map<Integer, Drawable> generateDrawableIconMap(Map<Integer, BoundaryColorSpan> paletteMap) {
        Map<Integer, Drawable> generatedDrawableMap = new HashMap<>(paletteMap.size());

        paletteMap.forEach((key, boundary) -> {
            try {
                int colorInt = boundary.color();
                Drawable drawableIcon = IconsUtil.getDrawableIconForAreaColorId(colorInt, 10, false);
                generatedDrawableMap.put(key, drawableIcon);
            } catch (Exception ex) {
                Log.e("DataEntityCurveEntry", "create: ", ex);
            }
        });

        return generatedDrawableMap;
    }

    /**
     * Gets the drawable icon corresponding to a specific value.
     * <p>
     * Determines which color boundary the value falls within and returns
     * the corresponding drawable icon.
     *
     * @param value The data value to find an icon for
     * @return The drawable icon associated with the value's color boundary
     */
    public Drawable getDrawableIconFrom(float value) {
        BoundaryColorSpan boundaryColorSpan = getBoundaryFrom(value);
        return drawableMap.get(boundaryColorSpan.id());
    }

    /**
     * Gets the entire palette map.
     *
     * @return The map of color boundaries
     */
    public Map<Integer, BoundaryColorSpan> getPalette() {
        return paletteMap;
    }

    /**
     * Determines which color boundary a specific value falls within.
     * <p>
     * This method normalizes the value relative to the data range and
     * calculates the appropriate boundary index.
     *
     * @param value The data value to find a boundary for
     * @return The color boundary the value falls within
     */
    public BoundaryColorSpan getBoundaryFrom(float value) {
        float normalizedVal = value - (float) dataEntityWrapper.getMinValue();

        BoundaryColorSpan first = paletteMap.entrySet().iterator().next().getValue();

        float delta = first.max() - first.min();

        int estimatedKeyIndex = (int) Math.floor(normalizedVal / delta);

        int estimatedKeyIndexLowIndexCheck = Math.max(estimatedKeyIndex, 0);

        int estimatedKeyIndexHighestIndexCheck = Math.min(estimatedKeyIndexLowIndexCheck, paletteMap.size() - 1);

        int finalEstimatedKeyIndex = estimatedKeyIndexHighestIndexCheck;

        BoundaryColorSpan objectFound = paletteMap.get(finalEstimatedKeyIndex);

        return objectFound;
    }

    /**
     * Checks if a value falls within a specific color boundary.
     *
     * @param value The value to check
     * @param boundaryColorSpan The boundary to check against
     * @return True if the value is within the boundary, false otherwise
     */
    public static boolean isWithinBoundary(float value, BoundaryColorSpan boundaryColorSpan) {
        return PrecisionUtil.isGreaterEqual(boundaryColorSpan.min(), value, PrecisionUtil.NDIG_PREC_COMP)
                &&
                value < boundaryColorSpan.max();
    }

    /**
     * Generates a palette of color boundaries based on the data range.
     * <p>
     * This method divides the range between min and max values into equal intervals
     * and assigns a color from the palette to each interval.
     *
     * @param min The minimum value in the data range
     * @param max The maximum value in the data range
     * @param numOfDividing The number of divisions to create
     * @param paletteFromBitmap The array of colors from the palette bitmap
     * @param paletteDirection The direction to read the palette (ascending or descending)
     * @return A map of color boundaries indexed by their position
     */
    private LinkedHashMap<Integer, BoundaryColorSpan> generatePalette(float min, float max, int numOfDividing, int[] paletteFromBitmap, PaletteDirection paletteDirection) {
        LinkedHashMap<Integer, BoundaryColorSpan> boundaryColorSpan = new LinkedHashMap<>();

        int maxYPalette = paletteFromBitmap.length - 1;

        float stepYPalette = maxYPalette / (float) numOfDividing;
        float valuesSpan = max - min;
        float stepValue = valuesSpan / (float) numOfDividing;

        float shiftFromStartValueOfBoundary = 0.5f * stepValue;

        for (int i = 0; i < numOfDividing; i++) {
            float value = i * stepValue + min;
            float nextValue = value + stepValue;

            float middleValueOfCurrentBoundary = value + shiftFromStartValueOfBoundary;
            float colorFloatScaledIndex = maxYPalette * ((middleValueOfCurrentBoundary - min) / valuesSpan);

            int indexColorStep = (int) Math.floor(colorFloatScaledIndex / stepYPalette);

            int colorFloatScaledIndexSteppedDiscrete = (int) (indexColorStep * stepYPalette);

            int scaledPixelColor = determineDiscreteColorFromScaledValue(maxYPalette, colorFloatScaledIndexSteppedDiscrete, paletteDirection);

            boundaryColorSpan.put(i, new BoundaryColorSpan(i, String.valueOf(value), value, nextValue, scaledPixelColor));
        }

        return boundaryColorSpan;
    }

    /**
     * Determines the actual color from the palette based on the scaled index.
     * <p>
     * This method handles different palette directions (ascending or descending)
     * when retrieving colors from the palette array.
     *
     * @param maxYPalette The maximum index in the palette array
     * @param colorFloatScaledIndexSteppedDiscrete The scaled and discretized index
     * @param paletteDirection The direction to read the palette
     * @return The integer color value from the palette
     */
    private int determineDiscreteColorFromScaledValue(int maxYPalette, int colorFloatScaledIndexSteppedDiscrete, PaletteDirection paletteDirection) {
        return switch (paletteDirection) {
            case MIN_IS_ZERO_INDEX_Y_PIXEL ->
                    paletteFromBitmap[colorFloatScaledIndexSteppedDiscrete];
            case MAX_IS_ZERO_INDEX_Y_PIXEL ->
                    paletteFromBitmap[maxYPalette - colorFloatScaledIndexSteppedDiscrete];
        };
    }
}
