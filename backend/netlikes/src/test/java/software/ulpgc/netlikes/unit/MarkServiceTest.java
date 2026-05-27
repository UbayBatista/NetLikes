package software.ulpgc.netlikes.unit;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MarkServiceTest {

    @Mock private MarkRepository markRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilmRepository filmRepository;

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private MarkService markService;

    private String email = "test@test.com";
    private Integer filmId = 1;

    @Test
    void toggleMarkLogic_CambiaDeWatchLaterASeen_Correctamente() {
        assertEquals("added", prepareMarkTest(Mark.Type.SEEN, false));
        verify(markRepository, times(1)).deleteByUserEmailAndFilmIdAndType(email, filmId, Mark.Type.WATCHLATER);
        verify(markRepository, times(1)).save(any(Mark.class));
    }

    @Test
    void toggleMarkLogic_AgregaRecomendacion_Correctamente() {
        assertEquals("added", prepareMarkTest(Mark.Type.RECOMMENDED, false));
        verify(markRepository, times(1)).save(any(Mark.class));
    }

    @Test
    void toggleMarkLogic_EliminaRecomendacion_SiYaExiste() {
        assertEquals("removed", prepareMarkTest(Mark.Type.RECOMMENDED, true));
        verify(markRepository, times(1)).deleteByUserEmailAndFilmIdAndType(email, filmId, Mark.Type.RECOMMENDED);
        verify(markRepository, never()).save(any(Mark.class));
    }

    @Test
    void testToggleRate_Seen_UpdatesUserVectorCorrectly() throws Exception {
        prepareMarkTest(Mark.Type.SEEN, false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User userSavedToDatabase = userCaptor.getValue();

        double[] actualVector = objectMapper.readValue(userSavedToDatabase.getVector(), double[].class);

        double[] expectedVector = {0.15, 0.85, 0.5};

        assertArrayEquals(expectedVector, actualVector, 0.0001);
    }

    @Test
    void testToggleRate_WatchLater_UpdatesUserVectorCorrectly() throws Exception {
        prepareMarkTest(Mark.Type.WATCHLATER, false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User userSavedToDatabase = userCaptor.getValue();

        double[] actualVector = objectMapper.readValue(userSavedToDatabase.getVector(), double[].class);

        double[] expectedVector = {0.05, 0.95, 0.5};

        assertArrayEquals(expectedVector, actualVector, 0.0001);
    }

    private String prepareMarkTest(Mark.Type mark, boolean action) {
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setVector("[0.0, 1.0, 0.5]");
        Film mockFilm = new Film();
        mockFilm.setId(filmId);
        mockFilm.setVector("[1.0, 0.0, 0.5]");

        when(userRepository.findById(email)).thenReturn(Optional.of(mockUser));
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(mockFilm));
        when(markRepository.existsByUserEmailAndFilmIdAndType(email, filmId, mark)).thenReturn(action);

        return markService.toggleMarkLogic(email, filmId, mark);
    }
}