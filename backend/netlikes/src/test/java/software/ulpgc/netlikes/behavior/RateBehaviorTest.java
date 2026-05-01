package software.ulpgc.netlikes.behavior;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.Rate;
import software.ulpgc.netlikes.model.RateId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.RateRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.RateService;

@SpringBootTest
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
        testUser.setUserName("UsuarioRate");
        testUser.setPassword("123");
        userRepository.save(testUser);

        testFilm = new Film();
        testFilm.setId(888);
        testFilm.setTitle("Pelicula Rate");
        filmRepository.save(testFilm);
    }

    @Test
    @DisplayName("HU 9.1: Valorar película vista")
    void shouldApplyNewRate() {
        String email = testUser.getEmail();
        Integer filmId = testFilm.getId();

        rateService.toggleRate(email, filmId, Rate.Score.LIKE);

        Optional<Rate> savedRate = rateRepository.findById(new RateId(email, filmId));
        assertThat(savedRate).isPresent();
        assertThat(savedRate.get().getScore()).isEqualTo(Rate.Score.LIKE);
    }

    @Test
    @DisplayName("HU 9.1: Modificar valoración existente")
    void shouldModifyExistingRate() {
        String email = testUser.getEmail();
        Integer filmId = testFilm.getId();
        rateService.toggleRate(email, filmId, Rate.Score.LIKE);

        rateService.toggleRate(email, filmId, Rate.Score.LOVE);

        Rate updatedRate = rateRepository.findById(new RateId(email, filmId)).get();
        assertThat(updatedRate.getScore()).isEqualTo(Rate.Score.LOVE);
    }

    @Test
    @DisplayName("HU 9.1: Eliminar valoración pulsando la misma")
    void shouldRemoveRateIfSameButtonClicked() {
        String email = testUser.getEmail();
        Integer filmId = testFilm.getId();
        rateService.toggleRate(email, filmId, Rate.Score.DISLIKE);

        Rate result = rateService.toggleRate(email, filmId, Rate.Score.DISLIKE);

        assertThat(result).isNull();
        assertThat(rateRepository.existsById(new RateId(email, filmId))).isFalse();
    }
}