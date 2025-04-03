package com.itservices.gpxanalyzer.ui.mapview;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

enum OverlayPriority { // Higher ordinal means more on top of overlays
    POLYLINES(Polyline.class),
    MARKERS(Marker.class);

    private final Class<?> clazz;

    OverlayPriority(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static OverlayPriority getFrom(Overlay overlay) {
        AtomicReference<OverlayPriority> found = new AtomicReference<>();

        Arrays.stream(values())
                .filter(overlayPriority -> overlayPriority.clazz.isInstance(overlay))
                .findFirst()
                .ifPresent(found::set);

        return found.get();
    }

    public static void sort(List<Overlay> overlays) {
        overlays.sort(
                Comparator.comparingInt(overlayComp ->
                        OverlayPriority.getFrom(overlayComp).ordinal()
                )
        );
    }
}
