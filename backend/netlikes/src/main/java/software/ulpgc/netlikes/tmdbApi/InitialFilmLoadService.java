package software.ulpgc.netlikes.tmdbApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import software.ulpgc.netlikes.service.FilmService;
import software.ulpgc.netlikes.service.HuggingFaceService;
import software.ulpgc.netlikes.dto.FilmRequestDTO;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitialFilmLoadService implements LoadService {

    private final TmdbApiClient apiClient;
    private final FilmAssembler filmAssembler;
    private final FilmService filmService;
    private final HuggingFaceService huggingFaceService;

    @Value("${tmdb.load.max-films:10}")
    private int maxFilms;

    @Override
    public void loadAll() {
        if (filmService.getAllFilms().size() > 0) {
            log.info("La base de datos ya tiene datos, omitiendo carga inicial");
            return;
        }
        loadFilms();
    }

    private void loadFilms() {
        List<Integer> ids = apiClient.getPopularFilmIds(maxFilms);
        int success = 0, failed = 0;

        for (int filmId : ids) {
            try {
                FilmRequestDTO dto = filmAssembler.toFilmRequestDTO(filmId);
                
                String textoParaVectorizar = String.format("Película: %s. Géneros: %s. Sinopsis: %s", 
                        dto.getTitle(), dto.getGenres(), dto.getOverView());

                String vector = huggingFaceService.generateVector(textoParaVectorizar);
                
                if (vector != null) {
                    dto.setVector(vector);
                    filmService.saveFilm(dto);
                    success++;
                    log.info("Película cargada y vectorizada con éxito: {}", dto.getTitle());
                } else {
                    log.warn("Se omitió la película {} porque falló la vectorización", filmId);
                    failed++;
                }

                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Error inesperado con película {}: {}", filmId, e.getMessage());
                failed++;
            }
        }

        log.info("Carga inicial completada: {} OK, {} fallidas", success, failed);
    }
}