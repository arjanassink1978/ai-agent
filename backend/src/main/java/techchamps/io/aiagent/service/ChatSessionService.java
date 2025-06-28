package techchamps.io.aiagent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import techchamps.io.aiagent.model.ChatMessage;
import techchamps.io.aiagent.model.ChatSession;
import techchamps.io.aiagent.repository.ChatMessageRepository;
import techchamps.io.aiagent.repository.ChatSessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChatSessionService {
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    public ChatSession createSession(String title, String context, String model, String imageModel) {
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(sessionId, title, context, model, imageModel);
        return chatSessionRepository.save(session);
    }
    
    public Optional<ChatSession> getSession(String sessionId) {
        return chatSessionRepository.findBySessionId(sessionId);
    }
    
    public List<ChatSession> getAllSessions() {
        return chatSessionRepository.findAllOrderByUpdatedAtDesc();
    }
    
    public List<ChatSession> searchSessions(String searchTerm) {
        return chatSessionRepository.findByTitleOrContextContaining(searchTerm);
    }
    
    public ChatSession updateSessionContext(String sessionId, String context) {
        Optional<ChatSession> optionalSession = chatSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isPresent()) {
            ChatSession session = optionalSession.get();
            session.setContext(context);
            session.setUpdatedAt(LocalDateTime.now());
            return chatSessionRepository.save(session);
        }
        throw new RuntimeException("Session not found: " + sessionId);
    }
    
    public ChatSession updateSessionTitle(String sessionId, String title) {
        Optional<ChatSession> optionalSession = chatSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isPresent()) {
            ChatSession session = optionalSession.get();
            session.setTitle(title);
            session.setUpdatedAt(LocalDateTime.now());
            return chatSessionRepository.save(session);
        }
        throw new RuntimeException("Session not found: " + sessionId);
    }
    
    public ChatMessage addMessage(String sessionId, String content, String sender, String imageUrl, String fileContent, String fileName) {
        Optional<ChatSession> optionalSession = chatSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isPresent()) {
            ChatSession session = optionalSession.get();
            ChatMessage message = new ChatMessage(content, sender);
            message.setImageUrl(imageUrl);
            message.setFileContent(fileContent);
            message.setFileName(fileName);
            session.addMessage(message);
            chatSessionRepository.save(session);
            return message;
        }
        throw new RuntimeException("Session not found: " + sessionId);
    }
    
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
    
    public void deleteSession(String sessionId) {
        chatSessionRepository.deleteBySessionId(sessionId);
    }
    
    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        List<ChatMessage> allMessages = chatMessageRepository.findBySessionIdOrderByTimestampDesc(sessionId);
        return allMessages.stream()
                .limit(limit)
                .sorted((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .toList();
    }
    
    public String getSessionContext(String sessionId) {
        Optional<ChatSession> session = chatSessionRepository.findBySessionId(sessionId);
        return session.map(ChatSession::getContext).orElse("");
    }
} 