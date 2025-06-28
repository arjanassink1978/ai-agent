package techchamps.io.aiagent.model;

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
    
    public static final List<String> IMAGE_MODELS = Arrays.asList(
        "dall-e-3",
        "dall-e-2"
    );
    
    public static final String DEFAULT_MODEL = "gpt-4";
    public static final String DEFAULT_IMAGE_MODEL = "dall-e-3";
    
    private String selectedModel;
    private String selectedImageModel;
    
    public ModelConfig() {
        this.selectedModel = DEFAULT_MODEL;
        this.selectedImageModel = DEFAULT_IMAGE_MODEL;
    }
    
    public ModelConfig(String selectedModel) {
        this.selectedModel = selectedModel != null ? selectedModel : DEFAULT_MODEL;
        this.selectedImageModel = DEFAULT_IMAGE_MODEL;
    }
    
    public ModelConfig(String selectedModel, String selectedImageModel) {
        this.selectedModel = selectedModel != null ? selectedModel : DEFAULT_MODEL;
        this.selectedImageModel = selectedImageModel != null ? selectedImageModel : DEFAULT_IMAGE_MODEL;
    }
    
    public String getSelectedModel() {
        return selectedModel;
    }
    
    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel != null ? selectedModel : DEFAULT_MODEL;
    }
    
    public String getSelectedImageModel() {
        return selectedImageModel;
    }
    
    public void setSelectedImageModel(String selectedImageModel) {
        this.selectedImageModel = selectedImageModel != null ? selectedImageModel : DEFAULT_IMAGE_MODEL;
    }
    
    public static List<String> getAvailableModels() {
        return AVAILABLE_MODELS;
    }
    
    public static List<String> getImageModels() {
        return IMAGE_MODELS;
    }
    
    public static String getDefaultModel() {
        return DEFAULT_MODEL;
    }
    
    public static String getDefaultImageModel() {
        return DEFAULT_IMAGE_MODEL;
    }
} 