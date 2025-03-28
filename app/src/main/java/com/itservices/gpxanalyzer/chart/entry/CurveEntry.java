package com.itservices.gpxanalyzer.chart.entry;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.itservices.gpxanalyzer.chart.settings.axis.HourMinutesAxisValueFormatter;
import com.itservices.gpxanalyzer.data.entity.DataEntity;
import com.itservices.gpxanalyzer.data.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.chart.legend.PaletteColorDeterminer;
import com.itservices.gpxanalyzer.data.cumulative.TrendBoundaryDataEntity;

import java.util.Calendar;

/**
 * Specialized chart entry representing a point within a trend boundary.
 * <p>
 * This class extends BaseEntry to represent data points that are part of trend boundaries
 * (such as ascent/descent segments or speed zones) in GPX tracks. CurveEntry objects 
 * include specific visual styling information and maintain a reference to their parent
 * trend boundary entity, allowing them to be grouped and visualized accordingly.
 * <p>
 * CurveEntries are typically created in batches through the factory method and are used
 * to visualize segments with similar characteristics (like uphill sections or speed zones)
 * on charts with consistent styling.
 */
public class CurveEntry extends BaseEntry {
    /**
     * Flag controlling whether to display icon indicators on curve entries.
     * When true, entries will display colored icons based on their data values.
     */
    public static boolean SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS = true;
    
    /**
     * The trend boundary entity this entry belongs to.
     * This maintains the relationship between the entry and its parent trend boundary
     * for grouping and styling purposes.
     */
    private final TrendBoundaryDataEntity trendBoundaryDataEntity;

    /**
     * Creates a new CurveEntry with the specified properties.
     * <p>
     * This constructor is package-private as entries should typically be created through
     * the static factory method which handles coordinate calculation and icon creation.
     *
     * @param dataEntity The data entity represented by this entry
     * @param trendBoundaryDataEntity The trend boundary this entry belongs to
     * @param x The x-coordinate (typically time as a float value)
     * @param y The y-coordinate (the measure value to display)
     * @param icon The icon to display at this entry point, or null for no icon
     * @param dataEntityWrapper The wrapper containing the dataset this entry belongs to
     */
    CurveEntry(
            DataEntity dataEntity, TrendBoundaryDataEntity trendBoundaryDataEntity, float x, float y, Drawable icon, DataEntityWrapper dataEntityWrapper
    ) {
        super(dataEntity, trendBoundaryDataEntity.id(), x, y, icon, dataEntityWrapper);

        this.trendBoundaryDataEntity = trendBoundaryDataEntity;
    }

    /**
     * Factory method to create a CurveEntry from a data entity and trend boundary.
     * <p>
     * This method handles the conversion of timestamp to chart-friendly x-coordinate,
     * extracts the appropriate y-value from the data entity, and creates an appropriate
     * icon based on the value and color palette.
     *
     * @param dataEntity The data entity to represent as a chart entry
     * @param trendBoundaryDataEntity The trend boundary this entry belongs to
     * @param paletteColorDeterminer The color palette provider for generating icons
     * @param dataEntityWrapper The wrapper containing the dataset being visualized
     * @return A new CurveEntry instance initialized with appropriate coordinates and styling
     */
    public static CurveEntry create(
            DataEntity dataEntity,
            TrendBoundaryDataEntity trendBoundaryDataEntity, PaletteColorDeterminer paletteColorDeterminer,
            DataEntityWrapper dataEntityWrapper
    ) {
        Drawable drawableIcon = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dataEntity.timestampMillis());
        float floatTime = HourMinutesAxisValueFormatter.combineIntoFloatTime(calendar);
        float value = dataEntityWrapper.getValue( dataEntity );

        try {
            drawableIcon = paletteColorDeterminer.getDrawableIconFrom(value);
        } catch (Exception ex) {
            Log.e("DataEntityCurveEntry", "create: ", ex);
        }

        return new CurveEntry(
                dataEntity,
                trendBoundaryDataEntity,
                floatTime, value, SHOW_COLOR_CURVE_DATA_ENTITY_RANGE_CIRCLES_ICONS ? drawableIcon : null,
                dataEntityWrapper
        );
    }

    /**
     * Gets the trend boundary entity this entry belongs to.
     * <p>
     * This method provides access to the parent trend boundary, which contains
     * information about the type of trend (e.g., ascent, descent, speed zone)
     * and styling properties for visualization.
     *
     * @return The TrendBoundaryDataEntity this entry is associated with
     */
    public TrendBoundaryDataEntity getTrendBoundaryDataEntity() {
        return trendBoundaryDataEntity;
    }
}
