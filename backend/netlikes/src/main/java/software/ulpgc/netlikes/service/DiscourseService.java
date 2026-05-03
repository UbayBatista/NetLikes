package software.ulpgc.netlikes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiscourseService {

    private final RestTemplate discourseRestTemplate;

    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Value("${discourse.category.movies.id}")
    private Integer moviesCategoryId;

    public DiscourseService(RestTemplate discourseRestTemplate) {
        this.discourseRestTemplate = discourseRestTemplate;
    }

    public Integer createMovieTopic(String movieTitle, Integer tmdbId) {
        Integer existingId = getForumIdByTitle(movieTitle);
        
        if (existingId != null) {
            return existingId; 
        }

        String uniqueRaw = "¡Bienvenido al foro oficial de la película **" + movieTitle + "**! \n\n¿Qué te ha parecido? Anímate a compartir tu opinión con otros usuarios suscritos. \n\n<!-- " + System.currentTimeMillis() + " -->";
        String endpoint = discourseUrl + "/posts.json";
        Map<String, Object> body = new HashMap<>();

        body.put("title", "Foro oficial: " + movieTitle); 
        body.put("raw", uniqueRaw);
        body.put("category", moviesCategoryId); 

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);

        try {
            ResponseEntity<Map> response = discourseRestTemplate.exchange(endpoint, HttpMethod.POST, request, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("topic_id")) {
                return (Integer) response.getBody().get("topic_id");
            }
        } catch (Exception e) {
            System.err.println("Error al crear foro en Discourse: " + e.getMessage());
        }
        return null;
    }

    public Integer getForumIdByTitle(String filmTitle) {
        String endpoint = discourseUrl + "/search.json?q=" + filmTitle;

        try {
            ResponseEntity<JsonNode> response = discourseRestTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                null, 
                JsonNode.class
            );

            JsonNode root = response.getBody();

            if (root != null && root.has("topics") && root.get("topics").isArray()) {
                String targetTitle = "Foro oficial: " + filmTitle;

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

    public Integer getTopicPostCount(Integer topicId) {
        String endpoint = discourseUrl + "/t/" + topicId + ".json";

        try {
            ResponseEntity<JsonNode> response = discourseRestTemplate.exchange(
                endpoint, 
                HttpMethod.GET, 
                null, 
                JsonNode.class
            );

            JsonNode root = response.getBody();

            if (root != null && root.has("posts_count")) {
                return root.get("posts_count").asInt();
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                return 0; 
            }
            System.err.println("Error al comunicar con la API de Discourse (Conteo de Posts): " + e.getMessage());
        }
        
        return 2; 
    }

    public void deleteTopic(Integer topicId, String originalTitle) {
        try {
            String renameEndpoint = discourseUrl + "/t/" + topicId + ".json";
            Map<String, Object> body = new HashMap<>();
            body.put("title", "Eliminado-" + System.currentTimeMillis() + "-" + originalTitle);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);
            discourseRestTemplate.exchange(renameEndpoint, HttpMethod.PUT, request, String.class);

            String deleteEndpoint = discourseUrl + "/t/" + topicId + ".json";
            discourseRestTemplate.delete(deleteEndpoint);
            
            System.out.println("Foro renombrado y eliminado correctamente. ID: " + topicId);
        } catch (Exception e) {
            System.err.println("Error al eliminar el foro en Discourse: " + e.getMessage());
        }
    }

}