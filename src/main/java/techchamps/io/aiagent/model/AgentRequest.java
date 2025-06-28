package techchamps.io.aiagent.model;

import java.util.Map;

public class AgentRequest {
    private String message;
    private String username;
    private String repository;
    private String personalAccessToken;
    private Map<String, Object> context;

    // Constructors
    public AgentRequest() {}

    public AgentRequest(String message, String username, String repository, String personalAccessToken) {
        this.message = message;
        this.username = username;
        this.repository = repository;
        this.personalAccessToken = personalAccessToken;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
} 