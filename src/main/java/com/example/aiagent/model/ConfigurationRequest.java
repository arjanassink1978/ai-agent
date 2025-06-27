package com.example.aiagent.model;

public class ConfigurationRequest {
    private String apiKey;
    private String model;
    
    public ConfigurationRequest() {
    }
    
    public ConfigurationRequest(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
} 