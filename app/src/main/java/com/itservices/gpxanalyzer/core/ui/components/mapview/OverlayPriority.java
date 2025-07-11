package com.itservices.gpxanalyzer.core.ui.components.mapview;

import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.IconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Defines the drawing priority (z-index) for different types of map overlays.
 * Overlays with higher ordinal values in this enum will be drawn on top of overlays with lower ordinal values.
 * This ensures, for example, that markers appear on top of polylines.
 */
enum OverlayPriority { // Higher ordinal means more on top of overlays
    /** Priority for Polyline overlays (drawn first, at the bottom). */
    POLYLINES(Polyline.class),
    /** Priority for Marker overlays. */
    MARKERS(Marker.class),
    /** Priority for general IconOverlay instances (if used). */
    MAP_ICON_OVERLAY(IconOverlay.class),
    /** Priority for the CopyrightOverlay (drawn last, on top of everything). */
    COPYRIGHT_OSM_OVERLAY(CopyrightOverlay.class);

    /** The class type associated with this priority level. */
    private final Class<?> clazz;

    /**
     * Enum constructor.
     *
     * @param clazz The {@link Overlay} subclass associated with this priority level.
     */
    OverlayPriority(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Determines the {@link OverlayPriority} for a given {@link Overlay} instance based on its class type.
     *
     * @param overlay The overlay instance.
     * @return The matching {@link OverlayPriority}, or null if the overlay type is not defined in the enum.
     */
    public static OverlayPriority getFrom(Overlay overlay) {
        AtomicReference<OverlayPriority> found = new AtomicReference<>();

        Arrays.stream(values())
                .filter(overlayPriority -> overlayPriority.clazz.isInstance(overlay))
                .findFirst()
                .ifPresent(found::set);

        return found.get();
    }

    /**
     * Sorts a list of {@link Overlay} objects based on their {@link OverlayPriority}.
     * Overlays with higher priority (higher ordinal) will appear later in the sorted list,
     * ensuring they are drawn on top when added to the map in the sorted order.
     *
     * @param overlays The list of {@link Overlay} objects to sort in-place.
     */
    public static void sort(List<Overlay> overlays) {
        overlays.sort(
                Comparator.comparingInt(overlayComp ->
                        OverlayPriority.getFrom(overlayComp).ordinal()
                )
        );
    }
}
