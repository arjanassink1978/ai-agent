package techchamps.io.aiagent.model;

import java.util.List;

public class SessionResponse {
    private String sessionId;
    private String title;
    private String context;
    private String model;
    private String imageModel;
    private List<ChatMessage> messages;
    private String error;
    private boolean success;

    public SessionResponse() {
    }

    public SessionResponse(String sessionId, String title, String context, String model, String imageModel) {
        this.sessionId = sessionId;
        this.title = title;
        this.context = context;
        this.model = model;
        this.imageModel = imageModel;
        this.success = true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
} 