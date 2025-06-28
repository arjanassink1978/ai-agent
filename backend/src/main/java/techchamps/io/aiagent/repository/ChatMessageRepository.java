package techchamps.io.aiagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import techchamps.io.aiagent.model.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.sessionId = :sessionId ORDER BY cm.timestamp ASC")
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(@Param("sessionId") String sessionId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.sessionId = :sessionId ORDER BY cm.timestamp DESC")
    List<ChatMessage> findBySessionIdOrderByTimestampDesc(@Param("sessionId") String sessionId);
    
    void deleteByChatSessionSessionId(String sessionId);
} 