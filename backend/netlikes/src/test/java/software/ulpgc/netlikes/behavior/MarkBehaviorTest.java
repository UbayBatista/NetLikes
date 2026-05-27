package software.ulpgc.netlikes.behavior;

import software.ulpgc.netlikes.config.TestConfig;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.MarkService;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Import(TestConfig.class)
public class MarkBehaviorTest {

    @Autowired private MarkService markService;
    @Autowired private MarkRepository markRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FilmRepository filmRepository;

    private User testUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test_mark@test.com");
        testUser.setName("UsuarioTest");
        testUser.setPassword("123");
        testUser.setBirthdate(java.sql.Date.valueOf("2000-01-01"));
        testUser.setSecurityQuestion("¿Nombre de tu mascota?");
        testUser.setAnswer("Toby");
        testUser.setAccountPrivacity(false);
        testUser.setShowWatchedFilms(false);
        testUser.setShowFilmsToWatchLater(false);
        testUser.setShowRecommendedFilms(false);
        testUser.setVector("[1.0, 0.5, 0.2]");
        userRepository.save(testUser);

        testFilm = new Film();
        testFilm.setId(999);
        testFilm.setTitle("Pelicula de Prueba");
        testFilm.setOverView("Sinopsis de prueba");
        testFilm.setAdult(false);
        testFilm.setPosterPath("/poster.jpg");
        testFilm.setReleaseDate(java.sql.Date.valueOf("2020-01-01"));
        testFilm.setRuntime(120);
        testFilm.setGenres(List.of());
        testFilm.setCast(new HashSet<>());
        testFilm.setVideos(List.of());
        testFilm.setVector("[0.8, 0.6, 0.4]");
        filmRepository.save(testFilm);
    }

    @Test
    @DisplayName("Should add film to seen list")
    void should_AddFilmToSeenList_when_ToggledToSeen() {
        assertThat(markRepository.existsById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN))).isFalse();

        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN);

        Optional<Mark> savedMark = markRepository.findById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN));
        assertThat(savedMark).isPresent();
        assertThat(savedMark.get().getType()).isEqualTo(Mark.Type.SEEN);
    }

    @Test
    @DisplayName("Should change from watch later to seen")
    void should_ChangeFromWatchLaterToSeen_when_ToggledToWatchLater() {
        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.WATCHLATER);
        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN);

        boolean existsSeen = markRepository.existsById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN));
        assertThat(existsSeen).isTrue();

        boolean existsWatchLater = markRepository.existsById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.WATCHLATER));
        assertThat(existsWatchLater).isFalse();
    }

    @Test
    @DisplayName("Should remove film from seen list")
    void should_RemoveFilmFromSeenList_when_ToggledOff() {
        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN);
        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN);

        assertThat(markRepository.existsById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.SEEN))).isFalse();
    }

    @Test
    @DisplayName("Should add film to recommended list")
    void should_AddFilmToRecommendedList_when_SelectedRecommended() {
        assertThat(markRepository.existsById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.RECOMMENDED))).isFalse();

        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.RECOMMENDED);

        Optional<Mark> savedMark = markRepository.findById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.RECOMMENDED));
        assertThat(savedMark).isPresent();
        assertThat(savedMark.get().getType()).isEqualTo(Mark.Type.RECOMMENDED);
    }

    @Test
    @DisplayName("Should remove film from recommended list")
    void should_RemoveFilmFromRecommendedList_when_UndoRecommended() {
        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.RECOMMENDED);

        markService.toggleMarkLogic(testUser.getEmail(), testFilm.getId(), Mark.Type.RECOMMENDED);

        assertThat(markRepository.existsById(new MarkId(testUser.getEmail(), testFilm.getId(), Mark.Type.RECOMMENDED))).isFalse();
    }
}