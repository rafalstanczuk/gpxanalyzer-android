package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for refreshing Strava OAuth 2.0 access tokens.
 * 
 * @see <a href="https://developers.strava.com/docs/authentication/#token-refresh">Strava Token Refresh Documentation</a>
 */
public class TokenRefreshRequest {
    
    @SerializedName("client_id")
    private String clientId;
    
    @SerializedName("client_secret")
    private String clientSecret;
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    @SerializedName("grant_type")
    private String grantType;
    
    /**
     * Constructor for token refresh request.
     * 
     * @param clientId The application's client ID
     * @param clientSecret The application's client secret
     * @param refreshToken The refresh token obtained from previous authorization
     */
    public TokenRefreshRequest(String clientId, String clientSecret, String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.grantType = "refresh_token";
    }
    
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getGrantType() {
        return grantType;
    }
    
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
    
    @Override
    public String toString() {
        return "TokenRefreshRequest{" +
                "clientId='" + clientId + '\'' +
                ", clientSecret='" + (clientSecret != null ? clientSecret.substring(0, Math.min(8, clientSecret.length())) + "..." : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(8, refreshToken.length())) + "..." : "null") + '\'' +
                ", grantType='" + grantType + '\'' +
                '}';
    }
} 