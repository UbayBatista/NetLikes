package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.transaction.Transactional;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.service.FilmService;
import software.ulpgc.netlikes.tmdbApi.LoadService;
import software.ulpgc.netlikes.tmdbApi.TmdbApiClient;
import software.ulpgc.netlikes.tmdbApi.TmdbModels;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

@SpringBootTest
public class UpdateFilmTest {

    @Autowired
    private FilmRepository filmrepository;

    @Autowired
    private FilmService filmservice;

    @Autowired
    private LoadService loadService;

    @MockitoBean
    private TmdbApiClient apiclient; 

    @BeforeEach
    void setUp() {
        filmrepository.deleteAll();
    }

    @Test
    void saveNewFilmFromApi() {
        
        when(apiclient.getPopularFilmIds(anyInt())).thenReturn(List.of(101, 102));

        TmdbModels.Film peli1 = new TmdbModels.Film(
            101,                  
            "Interstellar",       
            "Sinopsis de prueba", 
            false,                
            "16",                 
            "Un viaje espacial",  
            169,                  
            "2014-11-05",         
            "/poster1.jpg",     
            List.of(), 
            List.of(), 
            List.of(),
            List.of()  
        );

        TmdbModels.Film peli2 = new TmdbModels.Film(
            102,                  
            "Inception",          
            "Sueños dentro de sueños", 
            false,                
            "12",                 
            "Tu mente es la escena del crimen",  
            148,                  
            "2010-07-15",         
            "/poster2.jpg",       
            List.of(),
            List.of(), 
            List.of(), 
            List.of()  
        );
        
        when(apiclient.getCompleteFilm(101)).thenReturn(peli1);
        when(apiclient.getCompleteFilm(102)).thenReturn(peli2);

        assertThat(filmrepository.count()).isEqualTo(0);
        loadService.loadAll();
        List<Film> saveFilms = filmrepository.findAll();
        
        assertThat(saveFilms).hasSize(2);
        assertThat(saveFilms.get(0).getTitle()).isEqualTo("Interstellar");
        assertThat(saveFilms.get(1).getTitle()).isEqualTo("Inception");
    }

    @Test
    @Transactional
    void UpdateFilmCatalog(){
        
        Film filmsave1 = new Film();
        filmsave1.setId(101);
        filmsave1.setTitle("Matrix");
        filmsave1.setOverView("Un viaje espacial");
        filmsave1.setAdult(false);
        filmsave1.setReleaseDate(java.sql.Date.valueOf("2014-11-09"));      
        filmsave1.setPosterPath("b");      
        filmsave1.setRuntime(136);    
        filmsave1.setGenres(List.of());     
        filmsave1.setCast(Set.of());    
        filmsave1.setVideos(List.of()); 

        Film filmsave2 = new Film();
        filmsave2.setId(102);
        filmsave2.setTitle("Mario");
        filmsave2.setOverView("Tu mente es la escena del crimen");
        filmsave2.setAdult(false);
        filmsave2.setReleaseDate(java.sql.Date.valueOf("2014-11-05"));
        filmsave2.setPosterPath("a");
        filmsave2.setRuntime(136);
        filmsave2.setGenres(List.of());
        filmsave2.setCast(Set.of());    
        filmsave2.setVideos(List.of()); 

        filmrepository.save(filmsave1);
        filmrepository.save(filmsave2);

        List<FilmResponseDTO> catalogFilm = filmservice.getAllFilms();

        assertThat(catalogFilm).isNotNull();
        assertThat(catalogFilm).hasSize(2);

        assertThat(catalogFilm.stream().anyMatch(dto -> dto.getTitle().equals("Matrix"))).isTrue();
        assertThat(catalogFilm.stream().anyMatch(dto -> dto.getTitle().equals("Mario"))).isTrue();
        

    }
}