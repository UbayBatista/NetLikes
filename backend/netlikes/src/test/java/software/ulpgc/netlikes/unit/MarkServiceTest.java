package software.ulpgc.netlikes.unit;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MarkServiceTest {

    @Mock private MarkRepository markRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilmRepository filmRepository;

    @InjectMocks private MarkService markService;

    @Test
    void toggleMarkLogic_CambiaDeWatchLaterASeen_Correctamente() {
        String email = "test@test.com";
        Integer filmId = 1;
        User mockUser = new User();
        Film mockFilm = new Film();

        when(userRepository.findById(email)).thenReturn(Optional.of(mockUser));
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(mockFilm));
        
        when(markRepository.existsByUserEmailAndFilmIdAndType(email, filmId, Mark.Type.SEEN)).thenReturn(false);

        String result = markService.toggleMarkLogic(email, filmId, Mark.Type.SEEN);

        assertEquals("added", result);
        
        verify(markRepository, times(1)).deleteByUserEmailAndFilmIdAndType(email, filmId, Mark.Type.WATCHLATER);
        
        verify(markRepository, times(1)).save(any(Mark.class));
    }
}