package software.ulpgc.netlikes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import software.ulpgc.netlikes.service.TopicTrafficMonitorService;

@RestController
@RequestMapping("/api/discourse/webhooks")
public class DiscourseWebhookController {

    private final TopicTrafficMonitorService trafficMonitor;

    public DiscourseWebhookController(TopicTrafficMonitorService trafficMonitor) {
        this.trafficMonitor = trafficMonitor;
    }

    @PostMapping("/post-created")
    public ResponseEntity<String> handlePostCreated(
            @RequestBody JsonNode payload,
            @RequestHeader(value = "X-Discourse-Event", defaultValue = "unknown") String eventType) {

        if ("post_created".equals(eventType) && payload.has("post")) {
            JsonNode post = payload.get("post");
            
            if (post.has("post_type") && post.get("post_type").asInt() == 1) {
                int topicId = post.get("topic_id").asInt();
                trafficMonitor.registerNewMessage(topicId);
            }
        }
        
        return ResponseEntity.ok("OK");
    }
}
