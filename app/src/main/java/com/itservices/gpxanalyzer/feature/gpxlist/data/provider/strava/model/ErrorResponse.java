package com.itservices.gpxanalyzer.feature.gpxlist.data.provider.strava.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Error response model for Strava API errors.
 * 
 * @see <a href="https://developers.strava.com/docs/reference/#api-errors">Strava API Errors Documentation</a>
 */
public class ErrorResponse {
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("errors")
    private List<ErrorDetail> errors;
    
    // Constructors
    public ErrorResponse() {}
    
    public ErrorResponse(String message, List<ErrorDetail> errors) {
        this.message = message;
        this.errors = errors;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<ErrorDetail> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ErrorDetail> errors) {
        this.errors = errors;
    }
    
    /**
     * Checks if this is an authorization error (invalid or expired token).
     * 
     * @return true if it's an authorization error
     */
    public boolean isAuthorizationError() {
        if (errors == null || errors.isEmpty()) {
            return false;
        }
        
        for (ErrorDetail error : errors) {
            if ("access_token".equals(error.getField()) && "invalid".equals(error.getCode())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a formatted error message combining all error details.
     * 
     * @return Formatted error message
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (message != null && !message.isEmpty()) {
            sb.append(message);
        }
        
        if (errors != null && !errors.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(": ");
            }
            
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(errors.get(i).toString());
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
    
    /**
     * Individual error detail within an error response.
     */
    public static class ErrorDetail {
        
        @SerializedName("resource")
        private String resource;
        
        @SerializedName("field")
        private String field;
        
        @SerializedName("code")
        private String code;
        
        // Constructors
        public ErrorDetail() {}
        
        public ErrorDetail(String resource, String field, String code) {
            this.resource = resource;
            this.field = field;
            this.code = code;
        }
        
        // Getters and Setters
        public String getResource() {
            return resource;
        }
        
        public void setResource(String resource) {
            this.resource = resource;
        }
        
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        @Override
        public String toString() {
            return resource + "." + field + ": " + code;
        }
    }
} 