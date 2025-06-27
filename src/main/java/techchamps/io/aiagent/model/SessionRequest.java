package techchamps.io.aiagent.model;

public class SessionRequest {
    private String sessionId;
    private String title;
    private String context;
    private String model;
    private String imageModel;

    public SessionRequest() {
    }

    public SessionRequest(String sessionId, String title, String context, String model, String imageModel) {
        this.sessionId = sessionId;
        this.title = title;
        this.context = context;
        this.model = model;
        this.imageModel = imageModel;
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
} 