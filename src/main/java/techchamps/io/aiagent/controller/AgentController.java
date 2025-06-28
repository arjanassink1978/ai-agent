package techchamps.io.aiagent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import techchamps.io.aiagent.model.AgentRequest;
import techchamps.io.aiagent.model.AgentResponse;
import techchamps.io.aiagent.service.AgentService;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @PostMapping("/coding-buddy")
    public ResponseEntity<AgentResponse> processCodingBuddyRequest(@RequestBody AgentRequest request) {
        try {
            AgentResponse response = agentService.processNaturalLanguageRequest(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AgentResponse errorResponse = new AgentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error processing request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/tools/execute")
    public ResponseEntity<Map<String, Object>> executeTool(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String repository = (String) request.get("repository");
            Map<String, Object> toolCall = (Map<String, Object>) request.get("toolCall");
            
            Map<String, Object> result = agentService.executeTool(toolCall, username, repository);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
} 