package com.example.aiagent.model;

import java.util.List;
import java.util.Arrays;

public class ModelConfig {
    
    public static final List<String> AVAILABLE_MODELS = Arrays.asList(
        "gpt-4",
        "gpt-4-turbo", 
        "gpt-4o",
        "gpt-3.5-turbo",
        "gpt-3.5-turbo-16k"
    );
    
    public static final String DEFAULT_MODEL = "gpt-4";
    
    private String selectedModel;
    
    public ModelConfig() {
        this.selectedModel = DEFAULT_MODEL;
    }
    
    public ModelConfig(String selectedModel) {
        this.selectedModel = selectedModel != null ? selectedModel : DEFAULT_MODEL;
    }
    
    public String getSelectedModel() {
        return selectedModel;
    }
    
    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel != null ? selectedModel : DEFAULT_MODEL;
    }
    
    public static List<String> getAvailableModels() {
        return AVAILABLE_MODELS;
    }
    
    public static String getDefaultModel() {
        return DEFAULT_MODEL;
    }
} 