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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
public class DiscourseChatIT {
    
    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.url}")
    private String discourseUrl;

    String user1 = "ironman";
    String user2 = "netlikes.admin";

    @Test
    public void shouldSendChatMessage() {

        String userMenssage = "Este es un mensaje para un test";
        boolean successfulMessage  = sendChatMessage(user1, user2, userMenssage);   
        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

    @Test
    public void shouldSendChatVideo() {

        String userMenssage = "https://youtu.be/f7o-u153zGQ";
        boolean successfulMessage  = sendChatMessage(user1, user2, userMenssage);   
        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }

    @Test
    public void shouldSendChatImage() {

        String userMenssage = "https://images.daznservices.com/di/library/DAZN_News/91/8c/cristiano-ronaldo-champions-league_17ak1udoiuj631316hlbv1bi1i.png?t=37193869";
        boolean successfulMessage  = sendChatMessage(user1, user2, userMenssage);   
        assertTrue(successfulMessage, "El mensaje debería haberse publicado correctamente en Discourse");
    }


    private boolean sendChatMessage(String user1, String user2, String userMenssage){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", user1); 
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {

            String createChannelUrl = discourseUrl + "/chat/api/direct-message-channels.json";

            Map<String, Object> body = new HashMap<>();
            body.put("target_usernames", new String[]{user2});
            body.put("raw", userMenssage);

            HttpEntity<Map<String, Object>> channelRequest = new HttpEntity<>(body, headers);
            ResponseEntity<Map> channelResponse = restTemplate.postForEntity(createChannelUrl, channelRequest, Map.class);

            Map<String, Object> responseBody = channelResponse.getBody();
            Map<String, Object> channel = (Map<String, Object>) responseBody.get("channel");
            Integer channelId = (Integer) channel.get("id");

            String sendMessageUrl = discourseUrl + "/chat/" + channelId + ".json";
            
            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("message", userMenssage); 

            HttpEntity<Map<String, Object>> messageRequest = new HttpEntity<>(messageBody, headers);
            ResponseEntity<String> messageResponse = restTemplate.postForEntity(sendMessageUrl, messageRequest, String.class);

            return messageResponse.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("Fallo en el Test de Integración: " + e.getMessage());
            return false;
        }
    }

}
