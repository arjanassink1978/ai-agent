package techchamps.io.aiagent.model;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadRequest {
    private MultipartFile file;
    private String context; // "chat" or "image"
    private String prompt; // Optional prompt for image generation
    
    public FileUploadRequest() {
    }
    
    public FileUploadRequest(MultipartFile file, String context) {
        this.file = file;
        this.context = context;
    }
    
    public FileUploadRequest(MultipartFile file, String context, String prompt) {
        this.file = file;
        this.context = context;
        this.prompt = prompt;
    }
    
    public MultipartFile getFile() {
        return file;
    }
    
    public void setFile(MultipartFile file) {
        this.file = file;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
} 