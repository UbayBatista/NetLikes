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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class DiscourseService {


    @Value("${discourse.api.url}")
    private String discourseUrl;

    @Value("${discourse.category.movies.id}")
    private Integer moviesCategoryId;

    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.username}")
    private String apiUsername;

    private final RestTemplate restTemplate;

    public DiscourseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @NonNull
    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", apiKey);
        headers.set("Api-Username", apiUsername);
        return headers;
    }

    public Integer createMovieForum(String filmTitle) {
        RestTemplate restTemplate = new RestTemplate();
        String endpoint = discourseUrl + "/posts.json";

        setHeaders();
            
        Map<String, Object> body = new HashMap<>();
        
         String uniqueRaw = "¡Bienvenido al foro oficial de la película **" + filmTitle + "**! \n\n¿Qué te ha parecido? Anímate a compartir tu opinión con otros usuarios suscritos. \n\n<!-- " + System.currentTimeMillis() + " -->";

        body.put("title", "Foro oficial: " + filmTitle); 
        
        body.put("raw", uniqueRaw);
        
        body.put("category", moviesCategoryId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Integer) response.getBody().get("topic_id");
            }
        } catch (Exception e) {
            System.err.println("Error al crear foro en Discourse: " + e.getMessage());
        }
        return null;
    }

    // public Integer getForumIdByTitle(String filmTitle) {
    //     String endpoint = discourseUrl + "/search.json?q=" + filmTitle;

    //     try {
    //         ResponseEntity<JsonNode> response = restTemplate.exchange(
    //             endpoint, 
    //             HttpMethod.GET, 
    //             null, 
    //             JsonNode.class
    //         );

    //         JsonNode root = response.getBody();

    //         if (root != null && root.has("topics") && root.get("topics").isArray()) {
    //             String targetTitle = "Foro oficial: " + filmTitle;

    //             for (JsonNode topic : root.get("topics")) {
    //                 String topicTitle = topic.get("title").asText();
                    
    //                 if (topicTitle.equalsIgnoreCase(targetTitle)) {
    //                     return topic.get("id").asInt();
    //                 }
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.err.println("Error al comunicar con la API de Discourse (Búsqueda): " + e.getMessage());
    //     }
    //     return null;
    // }

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
            // 1. Obtener los datos actuales del usuario
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
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

    /**
     * Revierte un bloqueo en el foro.
     */
    // public void unignoreDiscourseUser(String blockerUsername, String unblockedUsername) {
    //     String url = discourseUrl + "/u/" + blockerUsername + ".json";
    //     HttpHeaders headers = setHeaders();

    //     try {
    //         ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    //         ObjectMapper mapper = new ObjectMapper();
    //         JsonNode root = mapper.readTree(response.getBody());
    //         JsonNode userNode = root.get("user");
            
    //         List<String> ignoredList = new ArrayList<>();
    //         if (userNode.has("ignored_usernames") && !userNode.get("ignored_usernames").isNull()) {
    //             userNode.get("ignored_usernames").forEach(node -> ignoredList.add(node.asText()));
    //         }

    //         if (ignoredList.contains(unblockedUsername)) {
    //             ignoredList.remove(unblockedUsername);
    //         } else {
    //             return;
    //         }

    //         Map<String, Object> body = new HashMap<>();
    //         body.put("ignored_usernames", ignoredList); 

    //         HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(body, headers);
    //         restTemplate.exchange(url, HttpMethod.PUT, putRequest, String.class);
    //         System.out.println("Usuario " + unblockedUsername + " desbloqueado en Discourse.");

    //     } catch (Exception e) {
    //         System.err.println("Error al desbloquear en Discourse: " + e.getMessage());
    //     }
    // }

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

    // public String createDiscourseUser(String name, String email, String password) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'createDiscourseUser'");
    // }

    public Integer getTopicPostCount(Integer topicId) {
        String endpoint = discourseUrl + "/t/" + topicId + ".json";

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
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
            restTemplate.exchange(renameEndpoint, HttpMethod.PUT, request, String.class);

            String deleteEndpoint = discourseUrl + "/t/" + topicId + ".json";
            restTemplate.delete(deleteEndpoint);
            
            System.out.println("Foro renombrado y eliminado correctamente. ID: " + topicId);
        } catch (Exception e) {
            System.err.println("Error al eliminar el foro en Discourse: " + e.getMessage());
        }
    }

}
