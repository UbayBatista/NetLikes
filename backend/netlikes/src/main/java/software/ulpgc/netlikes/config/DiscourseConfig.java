package software.ulpgc.netlikes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DiscourseConfig {

    @Value("${discourse.api.key}")
    private String apiKey;

    @Value("${discourse.api.username}")
    private String apiUsername;

    @Bean
    public RestTemplate discourseRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            request.getHeaders().add("Api-Key", apiKey);
            request.getHeaders().add("Api-Username", apiUsername);
            request.getHeaders().add("Content-Type", "application/json");
            request.getHeaders().add("Accept", "application/json");
            return execution.execute(request, body);
        };

        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (interceptors.isEmpty()) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }
}