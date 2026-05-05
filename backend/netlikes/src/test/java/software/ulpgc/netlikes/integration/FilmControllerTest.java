package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import software.ulpgc.netlikes.tmdbApi.FilmSyncScheduler;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class FilmControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private FilmRepository filmRepository;

    @MockitoBean private FilmSyncScheduler filmSyncScheduler;

    private Film createAndSaveFilm() {
        Film film = new Film();
        film.setId(102);
        film.setTitle("Mario");
        film.setOverView("Tu mente es la escena del crimen");
        film.setAdult(false);
        film.setReleaseDate(java.sql.Date.valueOf("2014-11-05"));
        film.setPosterPath("a");
        film.setRuntime(136);
        film.setGenres(List.of());
        film.setCast(new HashSet<>());
        film.setVideos(List.of());
        return filmRepository.save(film);
    }

    @Test
    void shouldReturnAllFilms() throws Exception {
        mockMvc.perform(get("/films"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnFilmDetailsAndVideos() throws Exception {
        Film savedFilm = createAndSaveFilm();

        mockMvc.perform(get("/films/" + savedFilm.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Mario"));
    }
}