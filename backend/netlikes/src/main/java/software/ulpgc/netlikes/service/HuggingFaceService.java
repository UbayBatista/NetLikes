package software.ulpgc.netlikes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class HuggingFaceService {

    @Value("${huggingface.api.token}")
    private String apiToken;

    private static final String API_URL = "https://router.huggingface.co/hf-inference/models/sentence-transformers/all-MiniLM-L6-v2/pipeline/feature-extraction";
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateVector(String text) {
        try {
            String jsonBody = mapper.writeValueAsString(new RequestBody(text));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            for (int i = 0; i < 3; i++) {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return response.body();
                } else if (response.statusCode() == 503) {
                    log.warn("El modelo de IA se está despertando (503). Reintentando en 5 segundos...");
                    Thread.sleep(5000);
                } else {
                    log.error("Error desde la API de Hugging Face. Status: {}, Body: {}", response.statusCode(), response.body());
                    return null;
                }
            }
            
            log.error("Se agotaron los reintentos esperando a que el modelo cargue.");
            return null;

        } catch (Exception e) {
            log.error("Excepción al intentar comunicar con Hugging Face", e);
            return null;
        }
    }

    private static class RequestBody {
        public String inputs;
        public RequestBody(String inputs) { this.inputs = inputs; }
    }
}