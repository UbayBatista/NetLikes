package software.ulpgc.netlikes.behavior;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import software.ulpgc.netlikes.service.FilmService;
import software.ulpgc.netlikes.config.TestConfig;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Import(TestConfig.class)
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
    @DisplayName("Should remove film from database when deleted")
    void should_RemoveFilmFromDatabase_when_Deleted() {
        createAndSaveFilm(101);

        filmRepository.deleteById(101);

        assertThat(filmRepository.existsById(101)).isFalse();
    }

    @Test
    @DisplayName("Should return empty video list when no trailers exist")
    void should_ReturnEmptyVideoList_when_NoTrailersExist() {
        createAndSaveFilm(102);

        FilmResponseDTO response = filmService.getFilmById(102);

        assertThat(response).isNotNull();
        assertThat(response.getVideos()).isEmpty();
    }
}