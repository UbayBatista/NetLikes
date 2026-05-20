package software.ulpgc.netlikes.behavior;

import software.ulpgc.netlikes.dto.RecommendCountDTO;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.RecommendService;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@Transactional
public class RecommendBehaviorTest {

    @Autowired private RecommendService recommendService;
    @Autowired private UserRepository userRepository;
    @Autowired private FilmRepository filmRepository;

    private User recommender;
    private User target1;
    private User target2;
    private Film testFilm;

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("User_" + email);
        user.setPassword("1234");
        user.setSecurityQuestion("¿Mascota?");
        user.setAnswer("Toby");
        user.setBirthdate(new Date());
        user.setAccountPrivacity(false);
        user.setShowWatchedFilms(false);
        user.setShowFilmsToWatchLater(false);
        user.setShowRecommendedFilms(false);
        user.setVector("");
        return userRepository.save(user);
    }

    private Film createFilm(Integer id) {
        Film film = new Film();
        film.setId(id);
        film.setTitle("Película " + id);
        film.setOverView("Sinopsis genérica");
        film.setAdult(false);
        film.setPosterPath("/ruta.jpg");
        film.setVector("");
        return filmRepository.save(film);
    }

    @BeforeEach
    void setUp() {
        recommender = this.createUser("recommender@test.com");
        target1 = this.createUser("target1@test.com");
        target2 = this.createUser("target2@test.com");
        testFilm = this.createFilm(100);
    }

    @Test
    @DisplayName("HU 10.1: Enviar recomendación a múltiples usuarios seguidos")
    void shouldSaveMultipleRecommendations() {
        recommendService.sendMultipleRecommendations(
            recommender.getEmail(), 
            testFilm.getId(), 
            List.of(target1.getEmail(), target2.getEmail())
        );

        List<Recommend> target1Recs = recommendService.getRecommendationsForUser(target1.getEmail());
        assertThat(target1Recs).hasSize(1);
        assertThat(target1Recs.get(0).getFilm().getId()).isEqualTo(testFilm.getId());

        List<Recommend> target2Recs = recommendService.getRecommendationsForUser(target2.getEmail());
        assertThat(target2Recs).hasSize(1);
    }

    @Test
    @DisplayName("HU 10.1: Obtener indicador de cuántas personas han recomendado la misma película")
    void shouldIncrementIndicatorWhenMultiplePeopleRecommendSameFilm() {
        recommendService.sendMultipleRecommendations(recommender.getEmail(), testFilm.getId(), List.of(target1.getEmail()));
        recommendService.sendMultipleRecommendations(target2.getEmail(), testFilm.getId(), List.of(target1.getEmail()));

        List<RecommendCountDTO> stats = recommendService.getRecommendedFilmsWithCount(target1.getEmail());

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getCount()).isEqualTo(2L);
        assertThat(stats.get(0).getFilm().getId()).isEqualTo(testFilm.getId());
    }
}