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
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.model.MarkId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.MarkRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.MarkService;

@SpringBootTest
@Transactional
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
        testUser.setUserName("UsuarioTest");
        testUser.setPassword("123");
        userRepository.save(testUser);

        testFilm = new Film();
        testFilm.setId(999);
        testFilm.setTitle("Pelicula de Prueba");
        filmRepository.save(testFilm);
    }

    @Test
    @DisplayName("HU 4.1: Añadir a Vistas")
    void shouldAddFilmToSeenList() {
        String email = testUser.getEmail();
        Integer filmId = testFilm.getId();
        assertThat(markRepository.existsById(new MarkId(email, filmId))).isFalse();

        markService.typeFilm(email, filmId, Mark.Type.SEEN);

        Optional<Mark> savedMark = markRepository.findById(new MarkId(email, filmId));
        assertThat(savedMark).isPresent();
        assertThat(savedMark.get().getType()).isEqualTo(Mark.Type.SEEN);
    }

    @Test
    @DisplayName("HU 4.1: Cambiar de Ver más tarde a Vistas")
    void shouldChangeFromWatchLaterToSeen() {
        String email = testUser.getEmail();
        Integer filmId = testFilm.getId();
        markService.typeFilm(email, filmId, Mark.Type.WATCHLATER);

        markService.typeFilm(email, filmId, Mark.Type.SEEN);

        Mark updatedMark = markRepository.findById(new MarkId(email, filmId)).get();
        assertThat(updatedMark.getType()).isEqualTo(Mark.Type.SEEN);
    }

    @Test
    @DisplayName("HU 4.1: Retirar de Vistas")
    void shouldRemoveFromSeenList() {
        String email = testUser.getEmail();
        Integer filmId = testFilm.getId();
        markService.typeFilm(email, filmId, Mark.Type.SEEN);

        markService.deletetype(email, filmId);

        assertThat(markRepository.existsById(new MarkId(email, filmId))).isFalse();
    }
}
