package techchamps.io.aiagent.model;

import java.util.List;

public class ImageResponse {
    private List<String> imageUrls;
    private String error;
    private String prompt;
    private String model;
    
    public ImageResponse() {
    }
    
    public ImageResponse(List<String> imageUrls, String prompt, String model) {
        this.imageUrls = imageUrls;
        this.prompt = prompt;
        this.model = model;
    }
    
    public ImageResponse(String error) {
        this.error = error;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
} 