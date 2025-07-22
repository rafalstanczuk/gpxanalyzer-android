package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for exchanging authorization code for access token in Strava OAuth 2.0 flow.
 * 
 * @see <a href="https://developers.strava.com/docs/authentication/#token-exchange">Strava Token Exchange Documentation</a>
 */
public class AuthorizationCodeRequest {
    
    @SerializedName("client_id")
    private String clientId;
    
    @SerializedName("client_secret")
    private String clientSecret;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("grant_type")
    private String grantType;
    
    /**
     * Constructor for authorization code exchange request.
     * 
     * @param clientId The application's client ID
     * @param clientSecret The application's client secret
     * @param code The authorization code received from the redirect
     */
    public AuthorizationCodeRequest(String clientId, String clientSecret, String code) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.code = code;
        this.grantType = "authorization_code";
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getGrantType() {
        return grantType;
    }
    
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
    
    @Override
    public String toString() {
        return "AuthorizationCodeRequest{" +
                "clientId='" + clientId + '\'' +
                ", clientSecret='" + (clientSecret != null ? clientSecret.substring(0, Math.min(8, clientSecret.length())) + "..." : "null") + '\'' +
                ", code='" + (code != null ? code.substring(0, Math.min(8, code.length())) + "..." : "null") + '\'' +
                ", grantType='" + grantType + '\'' +
                '}';
    }
} 