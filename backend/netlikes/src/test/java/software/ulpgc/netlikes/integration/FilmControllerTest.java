package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import software.ulpgc.netlikes.tmdbApi.FilmSyncScheduler;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;

import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class FilmControllerTest {

    @MockitoBean
    private FilmSyncScheduler filmSyncScheduler;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilmRepository filmRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldReturnAllFilms() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnFilmDetailsAndVideos() throws Exception {
        
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
        
        Film filmsave = filmRepository.save(film);

        mockMvc.perform(get("/films/" + filmsave.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Mario"));
    }
}