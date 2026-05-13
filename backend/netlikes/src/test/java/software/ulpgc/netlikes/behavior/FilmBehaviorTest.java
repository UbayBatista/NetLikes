package software.ulpgc.netlikes.behavior;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import software.ulpgc.netlikes.service.FilmService;

import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@Transactional
public class FilmBehaviorTest {

    @Autowired private FilmService filmService;
    @Autowired private FilmRepository filmRepository;

    private Film createAndSaveFilm(int id) {
        Film film = new Film();
        film.setId(id);
        film.setTitle("Test Movie");
        film.setOverView("Sinopsis de prueba");
        film.setPosterPath("/path.jpg");
        film.setReleaseDate(java.sql.Date.valueOf("2020-01-01"));
        film.setRuntime(120);
        film.setAdult(false);
        film.setGenres(new ArrayList<>());
        film.setWatchProviders(new ArrayList<>());
        film.setCast(new HashSet<>());
        film.setVideos(new ArrayList<>());
        film.setVector("");
        return filmRepository.save(film);
    }

    @Test
    @DisplayName("Eliminar una película debe borrarla de la base de datos")
    void deletingFilmShouldRemoveItFromDatabase() {
        createAndSaveFilm(101);

        filmService.deleteFilm(101);

        assertThat(filmRepository.existsById(101)).isFalse();
    }

    @Test
    @DisplayName("Debe devolver lista de vídeos vacía si la película no tiene trailers")
    void shouldReturnEmptyVideoListWhenNoTrailersExist() {
        createAndSaveFilm(102);

        FilmResponseDTO response = filmService.getFilmById(102);

        assertThat(response).isNotNull();
        assertThat(response.getVideos()).isEmpty();
    }
}