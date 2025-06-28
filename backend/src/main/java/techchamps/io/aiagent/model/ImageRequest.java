package techchamps.io.aiagent.model;

public class ImageRequest {
    private String prompt;
    private String model;
    private String size;
    private String quality;
    private String style;
    
    public ImageRequest() {
    }
    
    public ImageRequest(String prompt) {
        this.prompt = prompt;
    }
    
    public ImageRequest(String prompt, String model, String size, String quality, String style) {
        this.prompt = prompt;
        this.model = model;
        this.size = size;
        this.quality = quality;
        this.style = style;
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
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getQuality() {
        return quality;
    }
    
    public void setQuality(String quality) {
        this.quality = quality;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
} 