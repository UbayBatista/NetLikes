package software.ulpgc.netlikes.integration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
<<<<<<< HEAD:backend/netlikes/src/test/java/software/ulpgc/netlikes/integration/DiscourseIT.java
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
=======
>>>>>>> test/chats:backend/netlikes/src/test/java/software/ulpgc/netlikes/integration/DiscourseTest.java
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import software.ulpgc.netlikes.service.DiscourseService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

<<<<<<< HEAD:backend/netlikes/src/test/java/software/ulpgc/netlikes/integration/DiscourseIT.java

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class DiscourseIT{

=======

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class DiscourseTest{

>>>>>>> test/chats:backend/netlikes/src/test/java/software/ulpgc/netlikes/integration/DiscourseTest.java
    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Test
    @DisplayName("Should publish message successfully when valid inputs are provided")
    public void should_publishMessageSuccessfully_when_validInputsAreProvided() {
        
        int forumId = 161; 
        String username = "system";
        String userMenssage = "Este es un mensaje para un test";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

        
    @Test
    @DisplayName("Should publish image successfully when valid inputs are provided")
    public void should_publishImageSuccessfully_when_validInputsAreProvided() {
        
        int forumId = 161; 
        String username = "system";
        String userMenssage = "https://images.daznservices.com/di/library/DAZN_News/91/8c/cristiano-ronaldo-champions-league_17ak1udoiuj631316hlbv1bi1i.png?t=37193869";

        boolean successfulMessage  = sendMessage(forumId, userMenssage, username);

        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

        
    @Test
    @DisplayName("Should publish video successfully when valid inputs are provided")
    public void should_publishVideoSuccessfully_when_validInputsAreProvided() {
        
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