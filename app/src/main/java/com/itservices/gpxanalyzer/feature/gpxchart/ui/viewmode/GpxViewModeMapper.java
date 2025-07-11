package com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode;

import android.content.Context;

import androidx.annotation.StringRes;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;


/**
 * Maps {@link GpxViewMode} enum constants to the index of their corresponding primary data key
 * within a list of available data names (e.g., column headers from parsed GPX data).
 * This allows associating a chart view mode (like Altitude vs. Time) with the correct data column index.
 * Implements the {@link ViewModeMapper} interface.
 */
@Singleton
public class GpxViewModeMapper implements ViewModeMapper {

    /** Weak reference to the application context for accessing resources. */
    private final WeakReference<Context> contextWeakReference;

    /** Cache mapping GpxViewMode constants to their corresponding data column index. */
    private final Map<GpxViewMode, Integer> viewModeToPrimaryKeyIndex = new HashMap<>();

    /**
     * Constructor for dependency injection.
     *
     * @param context The application context, injected by Hilt.
     */
    @Inject
    public GpxViewModeMapper(@ApplicationContext Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    /**
     * Initializes the mapper by building the mapping between {@link GpxViewMode}s and their data indices.
     * It iterates through all {@code GpxViewMode} constants, retrieves the string resource ID for their
     * primary data key (e.g., R.string.altitude), finds the index of that string within the provided
     * {@code nameUnitList}, and stores the mapping.
     *
     * @param nameUnitList A list of strings representing the available data names (e.g., ["Timestamp", "Latitude", "Longitude", "Altitude", "Speed"]).
     *                     The order of this list determines the indices.
     */
    @Override
    public void init(List<String> nameUnitList) {
        for (GpxViewMode viewMode : GpxViewMode.values()) {
            viewModeToPrimaryKeyIndex.put(viewMode,
                    getNewPrimaryIndexFromNameStringRes(contextWeakReference.get(), nameUnitList, viewMode.getPrimaryKeyStringId())
            );
        }
    }

    /**
     * Retrieves the mapped primary data key index for a given view mode.
     * Expects the input {@code viewMode} to be an instance of {@link GpxViewMode}.
     *
     * @param viewMode The {@link Enum} representing the view mode (must be a {@link GpxViewMode}).
     * @return The integer index corresponding to the primary data key for the given view mode
     *         within the list provided during {@link #init(List)}, or -1 if the input is not a {@code GpxViewMode}.
     * @throws AssertionError if the index was not found (which shouldn't happen if {@code init} was called correctly).
     */
    @Override
    public int mapToPrimaryKeyIndexList(Enum<?> viewMode) {
        if (!(viewMode instanceof GpxViewMode)) {
            return -1;
        }

        Integer index = viewModeToPrimaryKeyIndex.get( (GpxViewMode) viewMode);

        assert index != null;

        return index;
    }

    /**
     * Finds the index of a string (obtained from a string resource ID) within a list of strings.
     *
     * @param context      The application context.
     * @param nameUnitList The list of data names to search within.
     * @param id           The string resource ID of the name to find.
     * @return The index of the string in the list, or -1 if not found.
     */
    private int getNewPrimaryIndexFromNameStringRes(Context context, List<String> nameUnitList, @StringRes int id) {
        return nameUnitList.indexOf(
                context.getResources().getString(id)
        );
    }
}
