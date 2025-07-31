package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

/**
 * Enum representing the available OAuth 2.0 scopes for the Strava API.
 * 
 * @see <a href="https://developers.strava.com/docs/authentication/#details-about-requesting-access">Strava API Scopes</a>
 */
public enum StravaScope {
    /**
     * Basic profile information
     */
    READ("read"),
    
    /**
     * All profile information
     */
    READ_ALL("read_all"),
    
    /**
     * Read access to activities
     */
    ACTIVITY_READ("activity:read"),
    
    /**
     * Read access to all activities (including private)
     */
    ACTIVITY_READ_ALL("activity:read_all"),
    
    /**
     * Write access to activities
     */
    ACTIVITY_WRITE("activity:write"),
    
    /**
     * Read all profile information
     */
    PROFILE_READ_ALL("profile:read_all"),
    
    /**
     * Write access to profile
     */
    PROFILE_WRITE("profile:write");
    
    private final String value;
    
    StravaScope(String value) {
        this.value = value;
    }
    
    /**
     * Returns the string value of the scope as expected by the Strava API.
     * 
     * @return The scope string value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Joins multiple scopes into a comma-separated string for API requests.
     * 
     * @param scopes The scopes to join
     * @return A comma-separated string of scope values
     */
    public static String join(StravaScope... scopes) {
        if (scopes == null || scopes.length == 0) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            builder.append(scopes[i].getValue());
            if (i < scopes.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
} 