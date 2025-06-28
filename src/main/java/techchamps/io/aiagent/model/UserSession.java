package techchamps.io.aiagent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sessionId;
    
    @Column(columnDefinition = "TEXT")
    private String githubToken;
    
    @Column
    private String githubUsername;
    
    @Column
    private String githubDisplayName;
    
    @Column
    private String selectedRepository;
    
    @Column
    private String repositoriesJson; // JSON array of repositories
    
    @Column
    private String openaiApiKey;
    
    @Column
    private String chatModel;
    
    @Column
    private String imageModel;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserSession() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserSession(String sessionId) {
        this();
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getGithubToken() {
        return githubToken;
    }
    
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
    
    public String getGithubUsername() {
        return githubUsername;
    }
    
    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }
    
    public String getGithubDisplayName() {
        return githubDisplayName;
    }
    
    public void setGithubDisplayName(String githubDisplayName) {
        this.githubDisplayName = githubDisplayName;
    }
    
    public String getSelectedRepository() {
        return selectedRepository;
    }
    
    public void setSelectedRepository(String selectedRepository) {
        this.selectedRepository = selectedRepository;
    }
    
    public String getRepositoriesJson() {
        return repositoriesJson;
    }
    
    public void setRepositoriesJson(String repositoriesJson) {
        this.repositoriesJson = repositoriesJson;
    }
    
    public String getOpenaiApiKey() {
        return openaiApiKey;
    }
    
    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }
    
    public String getChatModel() {
        return chatModel;
    }
    
    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }
    
    public String getImageModel() {
        return imageModel;
    }
    
    public void setImageModel(String imageModel) {
        this.imageModel = imageModel;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 