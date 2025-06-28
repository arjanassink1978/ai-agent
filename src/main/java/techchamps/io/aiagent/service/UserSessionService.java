package techchamps.io.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import techchamps.io.aiagent.model.UserSession;
import techchamps.io.aiagent.repository.UserSessionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSessionService {
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    public UserSession getOrCreateSession(String sessionId) {
        Optional<UserSession> existingSession = userSessionRepository.findBySessionId(sessionId);
        if (existingSession.isPresent()) {
            return existingSession.get();
        } else {
            UserSession newSession = new UserSession(sessionId);
            return userSessionRepository.save(newSession);
        }
    }
    
    public UserSession saveGithubToken(String sessionId, String token) {
        UserSession session = getOrCreateSession(sessionId);
        session.setGithubToken(token);
        return userSessionRepository.save(session);
    }
    
    public UserSession saveGithubUserInfo(String sessionId, String username, String displayName) {
        UserSession session = getOrCreateSession(sessionId);
        session.setGithubUsername(username);
        session.setGithubDisplayName(displayName);
        return userSessionRepository.save(session);
    }
    
    public UserSession saveSelectedRepository(String sessionId, String repository) {
        UserSession session = getOrCreateSession(sessionId);
        session.setSelectedRepository(repository);
        return userSessionRepository.save(session);
    }
    
    public UserSession saveRepositories(String sessionId, List<Object> repositories) {
        UserSession session = getOrCreateSession(sessionId);
        try {
            session.setRepositoriesJson(objectMapper.writeValueAsString(repositories));
        } catch (JsonProcessingException e) {
            // Handle error
        }
        return userSessionRepository.save(session);
    }
    
    public UserSession saveOpenAIConfig(String sessionId, String apiKey, String chatModel, String imageModel) {
        UserSession session = getOrCreateSession(sessionId);
        session.setOpenaiApiKey(apiKey);
        session.setChatModel(chatModel);
        session.setImageModel(imageModel);
        return userSessionRepository.save(session);
    }
    
    public void clearGithubData(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setGithubToken(null);
            session.setGithubUsername(null);
            session.setGithubDisplayName(null);
            session.setSelectedRepository(null);
            session.setRepositoriesJson(null);
            userSessionRepository.save(session);
        }
    }
    
    public void clearRepositoryData(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setSelectedRepository(null);
            userSessionRepository.save(session);
        }
    }
    
    public List<Object> getRepositories(String sessionId) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent() && sessionOpt.get().getRepositoriesJson() != null) {
            try {
                return objectMapper.readValue(sessionOpt.get().getRepositoriesJson(), List.class);
            } catch (JsonProcessingException e) {
                // Handle error
            }
        }
        return null;
    }
} 