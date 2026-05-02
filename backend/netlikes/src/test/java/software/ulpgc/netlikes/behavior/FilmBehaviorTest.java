package software.ulpgc.netlikes.behavior;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import software.ulpgc.netlikes.service.FilmService;
// import software.ulpgc.netlikes.api.FilmSyncScheduler;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.model.Film;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FilmBehaviorTest {

    @MockitoBean
    private FilmSyncScheduler filmSyncScheduler;

    @Autowired
    private FilmService filmService;
    
    @Autowired
    private FilmRepository filmRepository;

    @Test
    void deletingFilmShouldRemoveItFromDatabase() {
        filmService.deleteFilm(101); 
        
        assertThat(filmRepository.existsById(101)).isFalse();
    }

    @Test
    @Transactional
    void shouldReturnEmptyVideoListWhenNoTrailersExist() {
        Film film = new Film();
        film.setId(102);
        film.setTitle("Test Movie");
        film.setOverView("Sinopsis de prueba");
        film.setPosterPath("/path.jpg");
        film.setRuntime(120); 
        film.setAdult(false);
        
        film.setGenres(new ArrayList<>());
        film.setWatchProviders(new ArrayList<>());
        film.setCast(new HashSet<>());
        film.setVideos(new ArrayList<>());

        filmRepository.save(film);

        FilmResponseDTO response = filmService.getFilmById(102);
        
        assertNotNull(response);
        assertTrue(response.getVideos().isEmpty());
    }
}