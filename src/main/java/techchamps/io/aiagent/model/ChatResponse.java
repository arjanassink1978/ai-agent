package techchamps.io.aiagent.model;

import java.util.List;

public class ChatResponse {
    private String message;
    private String error;
    private String sessionId;
    private List<ChatMessage> messageHistory;
    private String context;

    public ChatResponse() {
    }

    public ChatResponse(String message) {
        this.message = message;
    }

    public ChatResponse(String message, String error) {
        this.message = message;
        this.error = error;
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
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public List<ChatMessage> getMessageHistory() {
        return messageHistory;
    }
    
    public void setMessageHistory(List<ChatMessage> messageHistory) {
        this.messageHistory = messageHistory;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
} 