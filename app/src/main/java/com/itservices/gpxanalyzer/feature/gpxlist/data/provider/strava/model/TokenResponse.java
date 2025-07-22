package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for Strava OAuth 2.0 token endpoints.
 * Used for both initial token exchange and token refresh responses.
 * 
 * @see <a href="https://developers.strava.com/docs/authentication/">Strava OAuth 2.0 Documentation</a>
 */
public class TokenResponse {
    
    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    @SerializedName("expires_at")
    private Long expiresAt;
    
    @SerializedName("expires_in")
    private Integer expiresIn;
    
    @SerializedName("token_type")
    private String tokenType;
    
    @SerializedName("athlete")
    private AthleteInfo athlete;
    
    // Constructors
    public TokenResponse() {}
    
    public TokenResponse(String accessToken, String refreshToken, Long expiresAt, Integer expiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public AthleteInfo getAthlete() {
        return athlete;
    }
    
    public void setAthlete(AthleteInfo athlete) {
        this.athlete = athlete;
    }
    
    /**
     * Checks if the access token is expired or will expire within the next 5 minutes.
     * 
     * @return true if token is expired or expiring soon
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return true;
        }
        
        // Add 5 minute buffer for token refresh
        long currentTimeWithBuffer = System.currentTimeMillis() / 1000 + 300;
        return expiresAt <= currentTimeWithBuffer;
    }
    
    /**
     * Gets the authorization header value for API calls.
     * 
     * @return Authorization header value (e.g., "Bearer abc123...")
     */
    public String getAuthorizationHeader() {
        if (accessToken == null || accessToken.isEmpty()) {
            return null;
        }
        return "Bearer " + accessToken;
    }
    
    @Override
    public String toString() {
        return "TokenResponse{" +
                "accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(8, accessToken.length())) + "..." : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(8, refreshToken.length())) + "..." : "null") + '\'' +
                ", expiresAt=" + expiresAt +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", athlete=" + athlete +
                '}';
    }
    
    /**
     * Basic athlete information returned with token response.
     */
    public static class AthleteInfo {
        @SerializedName("id")
        private Long id;
        
        @SerializedName("username")
        private String username;
        
        @SerializedName("resource_state")
        private Integer resourceState;
        
        @SerializedName("firstname")
        private String firstname;
        
        @SerializedName("lastname")
        private String lastname;
        
        @SerializedName("bio")
        private String bio;
        
        @SerializedName("city")
        private String city;
        
        @SerializedName("state")
        private String state;
        
        @SerializedName("country")
        private String country;
        
        @SerializedName("sex")
        private String sex;
        
        @SerializedName("premium")
        private Boolean premium;
        
        @SerializedName("summit")
        private Boolean summit;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;
        
        @SerializedName("badge_type_id")
        private Integer badgeTypeId;
        
        @SerializedName("weight")
        private Float weight;
        
        @SerializedName("profile_medium")
        private String profileMedium;
        
        @SerializedName("profile")
        private String profile;
        
        @SerializedName("friend")
        private String friend;
        
        @SerializedName("follower")
        private String follower;
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public Integer getResourceState() { return resourceState; }
        public String getFirstname() { return firstname; }
        public String getLastname() { return lastname; }
        public String getBio() { return bio; }
        public String getCity() { return city; }
        public String getState() { return state; }
        public String getCountry() { return country; }
        public String getSex() { return sex; }
        public Boolean getPremium() { return premium; }
        public Boolean getSummit() { return summit; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public Integer getBadgeTypeId() { return badgeTypeId; }
        public Float getWeight() { return weight; }
        public String getProfileMedium() { return profileMedium; }
        public String getProfile() { return profile; }
        public String getFriend() { return friend; }
        public String getFollower() { return follower; }
        
        @Override
        public String toString() {
            return "AthleteInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", firstname='" + firstname + '\'' +
                    ", lastname='" + lastname + '\'' +
                    '}';
        }
    }
} 