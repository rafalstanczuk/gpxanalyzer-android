package com.itservices.gpxanalyzer.ui.components.mapview;

/**
 * Configuration class containing default values and constants for map-related operations.
 * This class provides centralized configuration for map initialization, animations,
 * and visual elements such as polylines and padding.
 *
 * The class is designed to be used as a utility class with static constants and
 * cannot be instantiated.
 */
public class MapConfig {
    /** Default latitude coordinate (Warsaw, Poland) */
    public static final double DEFAULT_LATITUDE = 52.2297;
    
    /** Default longitude coordinate (Warsaw, Poland) */
    public static final double DEFAULT_LONGITUDE = 21.0122;
    
    /** Default zoom level for the map */
    public static final double DEFAULT_ZOOM_LEVEL = 12;
    
    /** Default padding in pixels for map boundaries */
    public static final int DEFAULT_PADDING_PX = 50;
    
    /** Duration of map animations in milliseconds */
    public static final long ANIMATION_DURATION_MS = 500;
    
    /** Default delay for map operations in milliseconds */
    public static final long DELAY_MS = 1000;
    
    /** Default width for polylines in pixels */
    public static final float DEFAULT_POLYLINE_WIDTH = 5f;
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MapConfig() {
        // Prevent instantiation
    }
} 