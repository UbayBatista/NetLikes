package software.ulpgc.netlikes.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; 

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.*;

@ExtendWith(MockitoExtension.class)
public class RateServiceTest {

    @Mock private RateRepository rateRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilmRepository filmRepository;

    @InjectMocks private RateService rateService;

    private User mockUser;
    private Film mockFilm;

    @BeforeEach
    void setUp() {
        mockUser = new User(); mockUser.setEmail("test@test.com");
        mockFilm = new Film(); mockFilm.setId(1);
    }

    @Test
    void toggleRate_NuevaValoracion_GuardaCorrectamente() {
        when(rateRepository.findById(any())).thenReturn(Optional.empty());
        when(userRepository.findById("test@test.com")).thenReturn(Optional.of(mockUser));
        when(filmRepository.findById(1)).thenReturn(Optional.of(mockFilm));
        when(rateRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Rate result = rateService.toggleRate("test@test.com", 1, Rate.Score.LIKE);

        assertNotNull(result);
        assertEquals(Rate.Score.LIKE, result.getScore());
        verify(rateRepository, times(1)).save(any());
    }

    @Test
    void toggleRate_MismaValoracion_EliminaValoracion() {
        Rate existingRate = new Rate(new RateId("test@test.com", 1), mockUser, mockFilm, Rate.Score.LOVE);
        when(rateRepository.findById(any())).thenReturn(Optional.of(existingRate));

        Rate result = rateService.toggleRate("test@test.com", 1, Rate.Score.LOVE);

        assertNull(result);
        verify(rateRepository, times(1)).delete(existingRate);
    }
}
