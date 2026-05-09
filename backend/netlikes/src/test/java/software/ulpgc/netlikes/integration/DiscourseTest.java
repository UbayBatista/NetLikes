package software.ulpgc.netlikes.integration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscourseTest{

    private final String discourseApiKey = "b6b3e96a0bffef725a4481481d8523e98f743a0ad861117370d6f08a1aa3173f"; 
    private final String discourseApiUrl = "https://netlikes.duckdns.org/posts.json";
    
    @Test
    public void shouldSendMessage() {
        
        int forumId = 112; 
        String username = "system";
        String userMenssage = "Este es un mensaje para un test";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

        
    @Test
    public void shouldSendImage() {
        
        int forumId = 112; 
        String username = "system";
        String userMenssage = "https://images.daznservices.com/di/library/DAZN_News/91/8c/cristiano-ronaldo-champions-league_17ak1udoiuj631316hlbv1bi1i.png?t=37193869";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

        
    @Test
    public void shouldSendvideo() {
        
        int forumId = 112; 
        String username = "system";
        String userMenssage = "https://youtu.be/f7o-u153zGQ";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

    private boolean sendMessage(int topicId, String menssage, String username) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", discourseApiKey);
        headers.set("Api-Username", username);

        Map<String, Object> body = new HashMap<>();
        body.put("topic_id", topicId);
        body.put("raw", menssage);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(discourseApiUrl, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error al contactar con Discourse: " + e.getMessage());
            return false;
        }
    }
}