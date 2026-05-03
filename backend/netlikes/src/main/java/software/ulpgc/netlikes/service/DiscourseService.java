package software.ulpgc.netlikes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscourseService {

    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.username}")
    private String apiUsername;
    
    private RestTemplate restTemplate = new RestTemplate();

    public Integer createMovieForum(String filmTitle) {
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

    public void deleteDiscourseUserById(String discourseId) {
        if (discourseId == null) {
            System.out.println("El usuario no tiene ID de Discourse asociado.");
            return;
        }

        String deleteUrl = discourseUrl + "/admin/users/" + discourseId + ".json";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", apiUsername);

        Map<String, Object> body = new HashMap<>();
        body.put("delete_posts", true);
        body.put("block_email", false);
        body.put("block_urls", false);
        body.put("block_ip", false);
        
        try {
            HttpEntity<Map<String, Object>> deleteRequest = new HttpEntity<>(body, headers);
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteRequest, String.class);
            
            System.out.println("Usuario eliminado de Discourse correctamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario en Discourse: " + e.getMessage());
        }
    }

    public void ignoreDiscourseUser(String blockerUsername, String blockedUsername) {
        String url = discourseUrl + "/u/" + blockerUsername + ".json";

        HttpHeaders headers = setHeaders();

        HttpEntity<String> getRequest = new HttpEntity<>(headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getRequest, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode userNode = root.get("user");
            List<String> ignoredList = new ArrayList<>();

            if (userNode.has("ignored_usernames") && !userNode.get("ignored_usernames").isNull()) {
                userNode.get("ignored_usernames").forEach(node -> ignoredList.add(node.asText()));
            }

            if (!ignoredList.contains(blockedUsername)) {
                ignoredList.add(blockedUsername);
            } else {
                System.out.println("El usuario ya estaba bloqueado en Discourse.");
                return; 
            }

            Map<String, Object> body = new HashMap<>();
            body.put("ignored_usernames", ignoredList); 

            HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PUT, putRequest, String.class);
            
            System.out.println("Lista de bloqueados actualizada para " + blockerUsername);

        } catch (Exception e) {
            System.err.println("Error en la lógica de bloqueo de Discourse: " + e.getMessage());
            throw new RuntimeException("No se pudo sincronizar el bloqueo con el foro.");
        }
    }

    public void unignoreDiscourseUser(String blockerUsername, String unblockedUsername) {
        String url = discourseUrl + "/u/" + blockerUsername + ".json";

        HttpHeaders headers = setHeaders();

        HttpEntity<String> getRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getRequest, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode userNode = root.get("user");
            
            List<String> ignoredList = new ArrayList<>();

            if (userNode.has("ignored_usernames") && !userNode.get("ignored_usernames").isNull()) {
                userNode.get("ignored_usernames").forEach(node -> ignoredList.add(node.asText()));
            }

            if (ignoredList.contains(unblockedUsername)) {
                ignoredList.remove(unblockedUsername);
            } else {
                System.out.println("El usuario no estaba en la lista de ignorados.");
                return; 
            }

            Map<String, Object> body = new HashMap<>();
            body.put("ignored_usernames", ignoredList); 

            HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PUT, putRequest, String.class);
            
            System.out.println("Usuario " + unblockedUsername + " desbloqueado en Discourse.");

        } catch (Exception e) {
            System.err.println("Error al desbloquear en Discourse: " + e.getMessage());
            throw new RuntimeException("No se pudo sincronizar el desbloqueo con el foro.");
        }
    }

    @NonNull
    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", apiUsername);
        return headers;
    }
}
