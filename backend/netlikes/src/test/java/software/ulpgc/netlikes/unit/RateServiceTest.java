package software.ulpgc.netlikes.unit;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
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
public class RateServiceTest {
    
    @Mock private RateRepository rateRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilmRepository filmRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private RateService rateService;

    private User mockUser;
    private Film mockFilm;

    @BeforeEach
    void setUp() {
        rateService = new RateService(rateRepository, userRepository, filmRepository, objectMapper);
        mockUser = new User(); 
        mockUser.setEmail("test@test.com");
        mockUser.setVector("[0.0, 1.0, 0.5]");
        mockFilm = new Film(); 
        mockFilm.setId(1);
        mockFilm.setVector("[1.0, 0.0, 0.5]");
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

    @Test
    void testToggleRate_Love_UpdatesUserVectorCorrectly() throws Exception {
        prepareToggleRateTestsWith(Rate.Score.LOVE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User userSavedToDatabase = userCaptor.getValue();

        double[] actualVector = objectMapper.readValue(userSavedToDatabase.getVector(), double[].class);

        double[] expectedVector = {0.3, 0.7, 0.5};

        assertArrayEquals(expectedVector, actualVector, 0.0001);
    }

    @Test
    void testToggleRate_Like_UpdatesUserVectorCorrectly() throws Exception {
        prepareToggleRateTestsWith(Rate.Score.LIKE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User userSavedToDatabase = userCaptor.getValue();

        double[] actualVector = objectMapper.readValue(userSavedToDatabase.getVector(), double[].class);

        double[] expectedVector = {0.15, 0.85, 0.5};

        assertArrayEquals(expectedVector, actualVector, 0.0001);
    }

    @Test
    void testToggleRate_Dislike_UpdatesUserVectorCorrectly() throws Exception {
        prepareToggleRateTestsWith(Rate.Score.DISLIKE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User userSavedToDatabase = userCaptor.getValue();

        double[] actualVector = objectMapper.readValue(userSavedToDatabase.getVector(), double[].class);

        double[] expectedVector = {-0.15, 0.85, 0.35};

        assertArrayEquals(expectedVector, actualVector, 0.0001);
    }

    void prepareToggleRateTestsWith(Rate.Score score) {
        String userEmail = "test@test.com";
        Integer filmId = 1;

        when(userRepository.findById(userEmail)).thenReturn(Optional.of(mockUser));
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(mockFilm));
        when(rateRepository.findById(any())).thenReturn(Optional.empty()); 

        rateService.toggleRate(userEmail, filmId, score);
    }
}
