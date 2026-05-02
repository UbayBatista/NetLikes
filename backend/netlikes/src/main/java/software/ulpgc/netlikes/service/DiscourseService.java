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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.lang.NonNull;

@Service
public class DiscourseService {


    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.username}")
    private String apiUsername;

    private final RestTemplate restTemplate;

    public DiscourseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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

    /**
     * Sincroniza un bloqueo: El blocker ignorará al blocked en el foro.
     * IMPORTANTE: Los usernames deben ser EXACTAMENTE los mismos que envías en el SSO.
     */
    public void ignoreDiscourseUser(String blockerUsername, String blockedUsername) {
        String url = discourseUrl + "/u/" + blockerUsername + ".json";
        HttpHeaders headers = setHeaders();

        try {
            // 1. Obtener los datos actuales del usuario
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode userNode = root.get("user");
            
            List<String> ignoredList = new ArrayList<>();
            if (userNode.has("ignored_usernames") && !userNode.get("ignored_usernames").isNull()) {
                userNode.get("ignored_usernames").forEach(node -> ignoredList.add(node.asText()));
            }

            // 2. Añadir el nuevo bloqueado si no está ya
            if (!ignoredList.contains(blockedUsername)) {
                ignoredList.add(blockedUsername);
            } else {
                System.out.println("El usuario ya estaba bloqueado en Discourse.");
                return; 
            }

            // 3. Enviar la lista actualizada a Discourse
            Map<String, Object> body = new HashMap<>();
            body.put("ignored_usernames", ignoredList); 

            HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PUT, putRequest, String.class);
            System.out.println("Lista de bloqueados actualizada en Discourse para " + blockerUsername);

        } catch (Exception e) {
            System.err.println("Error al ignorar usuario en Discourse: " + e.getMessage());
        }
    }

    /**
     * Revierte un bloqueo en el foro.
     */
    public void unignoreDiscourseUser(String blockerUsername, String unblockedUsername) {
        String url = discourseUrl + "/u/" + blockerUsername + ".json";
        HttpHeaders headers = setHeaders();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
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
                return;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("ignored_usernames", ignoredList); 

            HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.PUT, putRequest, String.class);
            System.out.println("Usuario " + unblockedUsername + " desbloqueado en Discourse.");

        } catch (Exception e) {
            System.err.println("Error al desbloquear en Discourse: " + e.getMessage());
        }
    }

    /**
     * Elimina a un usuario del foro cuando se da de baja en Netlikes.
     * Busca el ID internamente usando el username.
     */
    public void deleteDiscourseUserByUsername(String username) {
        try {
            // 1. Primero buscamos el ID interno de Discourse de ese usuario
            String getUserUrl = discourseUrl + "/u/" + username + ".json";
            ResponseEntity<String> response = restTemplate.exchange(getUserUrl, HttpMethod.GET, new HttpEntity<>(setHeaders()), String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            int discourseId = root.get("user").get("id").asInt();

            // 2. Con el ID, procedemos a borrarlo (o anonimizarlo)
            String deleteUrl = discourseUrl + "/admin/users/" + discourseId + ".json";
            
            Map<String, Object> body = new HashMap<>();
            body.put("delete_posts", true); // Borra también todo lo que escribió
            
            HttpEntity<Map<String, Object>> deleteRequest = new HttpEntity<>(body, setHeaders());
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteRequest, String.class);
            System.out.println("Usuario " + username + " eliminado de Discourse correctamente.");

        } catch (Exception e) {
            System.err.println("Error al eliminar usuario en Discourse: " + e.getMessage());
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

    public String createDiscourseUser(String name, String email, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createDiscourseUser'");
    }
}
