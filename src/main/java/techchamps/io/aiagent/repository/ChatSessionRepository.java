package techchamps.io.aiagent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import techchamps.io.aiagent.model.ChatSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    Optional<ChatSession> findBySessionId(String sessionId);
    
    @Query("SELECT cs FROM ChatSession cs ORDER BY cs.updatedAt DESC")
    List<ChatSession> findAllOrderByUpdatedAtDesc();
    
    @Query("SELECT cs FROM ChatSession cs WHERE cs.title LIKE %:searchTerm% OR cs.context LIKE %:searchTerm% ORDER BY cs.updatedAt DESC")
    List<ChatSession> findByTitleOrContextContaining(@Param("searchTerm") String searchTerm);
    
    void deleteBySessionId(String sessionId);
} 