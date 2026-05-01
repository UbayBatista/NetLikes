package software.ulpgc.netlikes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class DiscourseService {


    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.username}")
    private String apiUsername;

    /**
     * Crea un nuevo tema (foro) en Discourse para una película.
     * @param filmTitle El título de la película.
     * @return El ID del tema creado en Discourse (o null si falla).
     */

    public Integer createMovieForum(String filmTitle) {
        RestTemplate restTemplate = new RestTemplate();
        String endpoint = discourseUrl + "/posts.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", apiUsername);
            
        Map<String, Object> body = new HashMap<>();
        
        body.put("title", "Foro oficial: " + filmTitle); 
        
        body.put("raw", "Bienvenidos al foro oficial de la película " + filmTitle + ". ¡Podéis comentar vuestras opiniones y usar la etiqueta de spoilers si es necesario!");
        
        body.put("category", 5);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Integer) response.getBody().get("topic_id");
            }
        } catch (Exception e) {
            System.err.println("Error al comunicar con la API de Discourse: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un tema (foro) existente en Discourse por su título.
     * @param filmTitle El título original de la película.
     * @return El ID del tema encontrado en Discourse (o null si no existe).
     */
    public Integer getForumIdByTitle(String filmTitle) {
        RestTemplate restTemplate = new RestTemplate();
        
        String endpoint = discourseUrl + "/search.json?q=" + filmTitle;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", apiUsername);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                request, 
                JsonNode.class
            );

            JsonNode root = response.getBody();

            if (root != null && root.has("topics") && root.get("topics").isArray()) {
                
                String targetTitle = "Foro oficial: " + filmTitle;

                // Recorremos los resultados para encontrar el exacto
                for (JsonNode topic : root.get("topics")) {
                    String topicTitle = topic.get("title").asText();
                    
                    if (topicTitle.equalsIgnoreCase(targetTitle)) {
                        return topic.get("id").asInt();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al comunicar con la API de Discourse (Búsqueda): " + e.getMessage());
        }
        
        return null;
    }
}
