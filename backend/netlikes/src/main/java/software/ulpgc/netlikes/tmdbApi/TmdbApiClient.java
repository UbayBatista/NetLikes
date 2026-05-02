package software.ulpgc.netlikes.tmdbApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TmdbApiClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final int MAX_TMDB_PAGES = 500;
    private static final int MAX_RETRIES = 3;

    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TmdbApiClient(@Value("${tmdb.api.key}") String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<TmdbModels.Genre> getAllFilmGenres() {
        String endpoint = "/genre/movie/list?language=es-ES";
        TmdbModels.GenreListResponse response = makeRequest(endpoint, TmdbModels.GenreListResponse.class);
        
        return (response != null && response.genres() != null) 
               ? response.genres() 
               : Collections.emptyList();
    }

    public List<Integer> getPopularFilmIds(int quantity) {
        if (quantity <= 0) {
            return new ArrayList<>();
        }

        List<Integer> filmIds = new ArrayList<>();
        int currentPage = 1;

        while (filmIds.size() < quantity && currentPage <= MAX_TMDB_PAGES) {
            String endpoint = String.format("/discover/movie?language=es-ES&sort_by=popularity.desc&page=%d", currentPage);
            TmdbModels.DiscoverResponse response = makeRequest(endpoint, TmdbModels.DiscoverResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                break; 
            }

            for (TmdbModels.DiscoverResult result : response.results()) {
                filmIds.add(result.id());
            
                if (filmIds.size() == quantity) {
                    return filmIds;
                }
            }
            currentPage++;
        }

        return filmIds;
    }

    public TmdbModels.Film getCompleteFilm(int filmId) {
        String endpoint = "/movie/" + filmId + "?append_to_response=credits,videos,watch/providers,release_dates&language=es-ES";
        return makeRequest(endpoint, TmdbModels.Film.class);
    }

    public TmdbModels.Film getFilmDetails(int filmId) {
        return makeRequest("/movie/" + filmId + "?language=es-ES", TmdbModels.Film.class);
    }

    public TmdbModels.FilmCreditsResponse getFilmCredits(int filmId) {
        return makeRequest("/movie/" + filmId + "/credits?language=es-ES", TmdbModels.FilmCreditsResponse.class);
    }

    public TmdbModels.FilmVideosResponse getFilmVideos(int filmId) {
        return makeRequest("/movie/" + filmId + "/videos?language=es-ES", TmdbModels.FilmVideosResponse.class);
    }

    private <T> T makeRequest(String endpoint, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();
        
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), responseType);
                } 
                else if (response.statusCode() == 429) {
                    attempt++;
                    if (attempt >= MAX_RETRIES) {
                        throw new TmdbApiException("Límite de peticiones excedido (429) tras " + MAX_RETRIES + " intentos para: " + endpoint);
                    }
                    
                    long waitTimeMillis = response.headers().firstValueAsLong("Retry-After").orElse(1L) * 1000L;
                    System.out.println("Límite 429 alcanzado. Reintentando en " + waitTimeMillis + "ms... (Intento " + attempt + " de " + MAX_RETRIES + ")");
                    
                    Thread.sleep(waitTimeMillis);
                } 
                else if (response.statusCode() == 404) {
                    throw new TmdbApiException("Recurso no encontrado en TMDB (404) para: " + endpoint);
                } else {
                    throw new TmdbApiException("Error en API TMDB. Status: " + response.statusCode() + " - " + response.body());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TmdbApiException("La petición a TMDB fue interrumpida durante la espera de reintento.", e);
            } catch (Exception e) {
                if (e instanceof TmdbApiException) {
                    throw (TmdbApiException) e;
                }
                throw new TmdbApiException("Error de red/parseo al contactar con TMDB: " + e.getMessage(), e);
            }
        }
        
        throw new TmdbApiException("Fallo inesperado al realizar la petición a TMDB tras múltiples intentos.");
    }
}