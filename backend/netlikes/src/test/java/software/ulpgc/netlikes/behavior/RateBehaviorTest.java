package software.ulpgc.netlikes.behavior;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.RateService;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@Transactional
public class RateBehaviorTest {

    @Autowired private RateService rateService;
    @Autowired private RateRepository rateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FilmRepository filmRepository;

    private User testUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test_rate@test.com");
        testUser.setName("UsuarioRate");
        testUser.setPassword("123");
        testUser.setBirthdate(java.sql.Date.valueOf("2000-01-01"));
        testUser.setSecurityQuestion("¿Nombre de tu mascota?");
        testUser.setAnswer("Toby");
        userRepository.save(testUser);

        testFilm = new Film();
        testFilm.setId(888);
        testFilm.setTitle("Pelicula Rate");
        testFilm.setOverView("Sinopsis de prueba");
        testFilm.setAdult(false);
        testFilm.setPosterPath("/poster.jpg");
        testFilm.setReleaseDate(java.sql.Date.valueOf("2020-01-01"));
        testFilm.setRuntime(120);
        filmRepository.save(testFilm);
    }

    @Test
    @DisplayName("HU 9.1: Valorar película vista")
    void shouldApplyNewRate() {
        rateService.toggleRate(testUser.getEmail(), testFilm.getId(), Rate.Score.LIKE);

        Optional<Rate> savedRate = rateRepository.findById(new RateId(testUser.getEmail(), testFilm.getId()));
        assertThat(savedRate).isPresent();
        assertThat(savedRate.get().getScore()).isEqualTo(Rate.Score.LIKE);
    }

    @Test
    @DisplayName("HU 9.1: Modificar valoración existente")
    void shouldModifyExistingRate() {
        rateService.toggleRate(testUser.getEmail(), testFilm.getId(), Rate.Score.LIKE);
        rateService.toggleRate(testUser.getEmail(), testFilm.getId(), Rate.Score.LOVE);

        Rate updatedRate = rateRepository.findById(new RateId(testUser.getEmail(), testFilm.getId())).get();
        assertThat(updatedRate.getScore()).isEqualTo(Rate.Score.LOVE);
    }

    @Test
    @DisplayName("HU 9.1: Eliminar valoración pulsando la misma")
    void shouldRemoveRateIfSameButtonClicked() {
        rateService.toggleRate(testUser.getEmail(), testFilm.getId(), Rate.Score.DISLIKE);

        Rate result = rateService.toggleRate(testUser.getEmail(), testFilm.getId(), Rate.Score.DISLIKE);

        assertThat(result).isNull();
        assertThat(rateRepository.existsById(new RateId(testUser.getEmail(), testFilm.getId()))).isFalse();
    }
}