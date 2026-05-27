package software.ulpgc.netlikes.integration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import software.ulpgc.netlikes.service.DiscourseService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class DiscourseTest{

    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Test
    public void shouldSendMessage() {
        
        int forumId = 161; 
        String username = "system";
        String userMenssage = "Este es un mensaje para un test";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

        
    @Test
    public void shouldSendImage() {
        
        int forumId = 161; 
        String username = "system";
        String userMenssage = "https://images.daznservices.com/di/library/DAZN_News/91/8c/cristiano-ronaldo-champions-league_17ak1udoiuj631316hlbv1bi1i.png?t=37193869";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

        
    @Test
    public void shouldSendvideo() {
        
        int forumId = 161; 
        String username = "system";
        String userMenssage = "https://youtu.be/f7o-u153zGQ";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

    private boolean sendMessage(int topicId, String menssage, String username) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", username);

        Map<String, Object> body = new HashMap<>();
        body.put("topic_id", topicId);
        body.put("raw", menssage);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            String url = discourseUrl.endsWith("/posts.json") ? discourseUrl : discourseUrl + "/posts.json";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error al contactar con Discourse: " + e.getMessage());
            return false;
        }
    }
}