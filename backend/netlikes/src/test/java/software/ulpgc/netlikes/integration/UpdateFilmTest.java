package software.ulpgc.netlikes.integration;

import software.ulpgc.netlikes.api.LoadService;
import software.ulpgc.netlikes.api.TmdbApiClient;
import software.ulpgc.netlikes.api.TmdbModels;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.service.FilmService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UpdateFilmTest {

    @Autowired private FilmRepository filmRepository;
    @Autowired private FilmService filmService;
    @Autowired private LoadService loadService;

    @MockitoBean private TmdbApiClient apiClient;

    @Test
    void saveNewFilmFromApi() {
        when(apiClient.getPopularFilmIds(anyInt())).thenReturn(List.of(101, 102));

        TmdbModels.Film film1 = new TmdbModels.Film(
            101, "Interstellar", "Sinopsis de prueba",
            false, "16", "Un viaje espacial",
            169, "2014-11-05", "/poster1.jpg",
            List.of(), List.of(), List.of(), List.of()
        );

        TmdbModels.Film film2 = new TmdbModels.Film(
            102, "Inception", "Sueños dentro de sueños",
            false, "12", "Tu mente es la escena del crimen",
            148, "2010-07-15", "/poster2.jpg",
            List.of(), List.of(), List.of(), List.of()
        );

        when(apiClient.getCompleteFilm(101)).thenReturn(film1);
        when(apiClient.getCompleteFilm(102)).thenReturn(film2);

        assertThat(filmRepository.count()).isEqualTo(0);

        loadService.loadAll();

        List<Film> savedFilms = filmRepository.findAll();
        assertThat(savedFilms).hasSize(2);
        assertThat(savedFilms.get(0).getTitle()).isEqualTo("Interstellar");
        assertThat(savedFilms.get(1).getTitle()).isEqualTo("Inception");
    }

    @Test
    void updateFilmCatalog() {
        Film film1 = new Film();
        film1.setId(101);
        film1.setTitle("Matrix");
        film1.setOverView("Un viaje espacial");
        film1.setAdult(false);
        film1.setReleaseDate(java.sql.Date.valueOf("2014-11-09"));
        film1.setPosterPath("b");
        film1.setRuntime(136);
        film1.setGenres(List.of());
        film1.setCast(Set.of());
        film1.setVideos(List.of());

        Film film2 = new Film();
        film2.setId(102);
        film2.setTitle("Mario");
        film2.setOverView("Tu mente es la escena del crimen");
        film2.setAdult(false);
        film2.setReleaseDate(java.sql.Date.valueOf("2014-11-05"));
        film2.setPosterPath("a");
        film2.setRuntime(136);
        film2.setGenres(List.of());
        film2.setCast(Set.of());
        film2.setVideos(List.of());

        filmRepository.save(film1);
        filmRepository.save(film2);

        List<FilmResponseDTO> catalog = filmService.getAllFilms();

        assertThat(catalog).isNotNull();
        assertThat(catalog).hasSize(2);
        assertThat(catalog.stream().anyMatch(dto -> dto.getTitle().equals("Matrix"))).isTrue();
        assertThat(catalog.stream().anyMatch(dto -> dto.getTitle().equals("Mario"))).isTrue();
    }
}