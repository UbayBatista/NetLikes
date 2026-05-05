package software.ulpgc.netlikes.tmdbApi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TmdbModels {

    // MODELOS DE DATOS ESPECÍFICOS

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genre(@JsonProperty("id") int id, @JsonProperty("name") String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CastMember(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("profile_path") String profilePath,
        @JsonProperty("character") String character
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Video(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("key") String key,
        @JsonProperty("type") String type,
        @JsonProperty("site") String site
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Provider(
        @JsonProperty("provider_id") int providerId,
        @JsonProperty("provider_name") String providerName,
        @JsonProperty("logo_path") String logoPath
    ) {}

    // MODELOS PARA RESPUESTAS DE DISCOVER Y GÉNEROS

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiscoverResponse(
        @JsonProperty("page") int page,
        @JsonProperty("results") List<DiscoverResult> results,
        @JsonProperty("total_pages") int totalPages
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiscoverResult(@JsonProperty("id") int id) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenreListResponse(@JsonProperty("genres") List<Genre> genres) {}

    // CLASES INTERMEDIAS PARA LA CALIFICACIÓN DE EDAD

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReleaseDatesNode(@JsonProperty("results") List<ReleaseDateCountry> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReleaseDateCountry(
        @JsonProperty("iso_3166_1") String countryCode,
        @JsonProperty("release_dates") List<ReleaseDateItem> releaseDates
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReleaseDateItem(
        @JsonProperty("certification") String certification
    ) {}

    // MODELOS PARA RESPUESTAS INDIVIDUALES
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilmCreditsResponse(
        @JsonProperty("id") int id,
        @JsonProperty("cast") List<CastMember> cast
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FilmVideosResponse(
        int id,
        List<Video> results
    ) {
        @JsonCreator
        public static FilmVideosResponse fromJson(
            @JsonProperty("id") int id,
            @JsonProperty("results") List<Video> results
        ) {
            List<Video> youtubeVideos = results != null 
                ? results.stream().filter(v -> "YouTube".equalsIgnoreCase(v.site())).toList() 
                : Collections.emptyList();
            return new FilmVideosResponse(id, youtubeVideos);
        }
    }

    // CLASES INTERMEDIAS PARA JACKSON (Solo uso interno)
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CreditsNode(@JsonProperty("cast") List<CastMember> cast) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VideosNode(@JsonProperty("results") List<Video> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ProvidersNode(@JsonProperty("results") Map<String, ProviderCountryData> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ProviderCountryData(
        @JsonProperty("flatrate") List<Provider> flatrate,
        @JsonProperty("rent") List<Provider> rent,
        @JsonProperty("buy") List<Provider> buy
    ) {}

    // EL OBJETO FILM PLANO FINAL

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Film(
        int id,
        String title,
        String overview,
        boolean adult,
        String ageRating,
        String tagline,
        Integer runtime,
        String releaseDate,
        String posterPath,
        List<Genre> genres,
        List<CastMember> cast,
        List<Video> videos,
        List<Provider> watchProviders
    ) {

        @JsonCreator
        public static Film fromJson(
            @JsonProperty("id") int id,
            @JsonProperty("title") String title,
            @JsonProperty("overview") String overview,
            @JsonProperty("adult") boolean adult,
            @JsonProperty("tagline") String tagline,
            @JsonProperty("runtime") Integer runtime,
            @JsonProperty("release_date") String releaseDate,
            @JsonProperty("poster_path") String posterPath,
            @JsonProperty("genres") List<Genre> genres,
            
            // Nodos anidados para aplanar
            @JsonProperty("credits") CreditsNode credits,
            @JsonProperty("videos") VideosNode videos,
            @JsonProperty("watch/providers") ProvidersNode providers,
            @JsonProperty("release_dates") ReleaseDatesNode releaseDates
        ) {
            List<CastMember> extractedCast = credits != null && credits.cast() != null ? credits.cast() : Collections.emptyList();
            
            List<Video> extractedVideos = videos != null && videos.results() != null 
                ? videos.results().stream()
                    .filter(v -> "YouTube".equalsIgnoreCase(v.site()))
                    .toList() 
                : Collections.emptyList();
            
            // Extracción específica de Providers para España (ES) uniendo las diferentes opciones (flatrate, rent, buy)
            List<Provider> extractedProviders = new ArrayList<>();
            if (providers != null && providers.results() != null && providers.results().containsKey("ES")) {
                ProviderCountryData esData = providers.results().get("ES");
                
                // Combinamos todas las formas de visionado y eliminamos posibles duplicados usando distinct por provider_id
                extractedProviders = Stream.of(esData.flatrate(), esData.rent(), esData.buy())
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        // Evita que salga Netflix 2 veces si está en suscripción y en compra
                        .collect(Collectors.toMap(Provider::providerId, p -> p, (existing, replacement) -> existing))
                        .values().stream().toList();
            }

            String extractedAgeRating = "NR"; // Not Rated por defecto
            if (releaseDates != null && releaseDates.results() != null) {
                extractedAgeRating = releaseDates.results().stream()
                    .filter(country -> "ES".equals(country.countryCode()))
                    .findFirst()
                    .flatMap(country -> country.releaseDates().stream()
                        // Buscamos el primer certificado que contenga información
                        .filter(dateItem -> dateItem.certification() != null && !dateItem.certification().isBlank())
                        .findFirst()
                        .map(ReleaseDateItem::certification))
                    .orElse("NR");
            }

            return new Film(
                id, title, overview, adult, extractedAgeRating, tagline, runtime,
                releaseDate, posterPath,
                genres != null ? genres : Collections.emptyList(), 
                extractedCast, extractedVideos, extractedProviders
            );
        }
    }
}