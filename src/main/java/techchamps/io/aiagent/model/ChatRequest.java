package techchamps.io.aiagent.model;

import java.util.Map;
import java.util.HashMap;

public class ChatRequest {
    private String message;
    private String sessionId;
    private String context;
    private String model;
    private String imageModel;
    private String fileContent;
    private String fileName;
    private Map<String, Object> metadata;

    public ChatRequest() {
        this.metadata = new HashMap<>();
    }

    public ChatRequest(String message) {
        this.message = message;
        this.metadata = new HashMap<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getImageModel() {
        return imageModel;
    }
    
    public void setImageModel(String imageModel) {
        this.imageModel = imageModel;
    }
    
    public String getFileContent() {
        return fileContent;
    }
    
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
} 