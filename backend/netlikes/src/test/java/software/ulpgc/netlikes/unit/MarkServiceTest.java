package software.ulpgc.netlikes.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.*;

@ExtendWith(MockitoExtension.class)
public class MarkServiceTest {

    @Mock private MarkRepository markRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilmRepository filmRepository;

    @InjectMocks private MarkService markService;

    @Test
    void typeFilm_CambiaDeEstadoCorrectamente() {
        Mark existingMark = new Mark();
        existingMark.setType(Mark.Type.WATCHLATER);
        
        when(markRepository.findById(any())).thenReturn(Optional.of(existingMark));
        when(markRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Mark result = markService.typeFilm("test@test.com", 1, Mark.Type.SEEN);

        assertEquals(Mark.Type.SEEN, result.getType());
        verify(markRepository, times(1)).save(existingMark);
    }
}
