package software.ulpgc.netlikes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        String endpoint = discourseUrl + "/posts.json";

        Map<String, Object> body = new HashMap<>();
        body.put("title", movieTitle + " - Debate Oficial"); 
        body.put("raw", "¡Bienvenido al foro oficial de la película **" + movieTitle + "**! \n\n¿Qué te ha parecido? Anímate a compartir tu opinión con otros usuarios suscritos.");
        body.put("category", moviesCategoryId); 

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);

        try {
            ResponseEntity<Map> response = discourseRestTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("topic_id")) {
                return (Integer) response.getBody().get("topic_id");
            }
        } catch (Exception e) {
            System.err.println("Error al crear el foro en Discourse para la película: " + movieTitle);
            e.printStackTrace();
        }
        
        return null;
    }
}
