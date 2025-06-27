package techchamps.io.aiagent.model;

import java.util.List;

public class FileUploadResponse {
    private String message;
    private String error;
    private String fileName;
    private String fileUrl;
    private List<String> imageUrls; // For image generation results
    private String context;
    
    public FileUploadResponse() {
    }
    
    public FileUploadResponse(String message, String fileName, String context) {
        this.message = message;
        this.fileName = fileName;
        this.context = context;
    }
    
    public FileUploadResponse(String error) {
        this.error = error;
    }
    
    public FileUploadResponse(String message, String fileName, List<String> imageUrls, String context) {
        this.message = message;
        this.fileName = fileName;
        this.imageUrls = imageUrls;
        this.context = context;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
} 