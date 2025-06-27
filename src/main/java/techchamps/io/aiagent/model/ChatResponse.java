package techchamps.io.aiagent.model;

public class ChatResponse {
    private String message;
    private String error;

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
} 