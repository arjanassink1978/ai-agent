package techchamps.io.aiagent.model;

public class RepositoryConnectRequest {
    private String personalAccessToken;
    private String repositoryUrl;

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public String toString() {
        return "RepositoryConnectRequest{" +
                "personalAccessToken='" + (personalAccessToken != null ? "present (length: " + personalAccessToken.length() + ")" : "null") + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                '}';
    }
} 