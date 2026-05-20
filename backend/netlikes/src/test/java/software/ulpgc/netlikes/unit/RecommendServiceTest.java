package software.ulpgc.netlikes.unit;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;
import software.ulpgc.netlikes.service.RecommendService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RecommendServiceTest {

    @Mock private RecommendRepository recommendRepository;
    @Mock private UserRepository userRepository;
    @Mock private FilmRepository filmRepository;

    @InjectMocks private RecommendService recommendService;

    private User createMockUser(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }

    private Film createMockFilm(Integer id) {
        Film film = new Film();
        film.setId(id);
        return film;
    }

    @Test
    void sendMultipleRecommendations_SavesCorrectAmountOfTimes() {
        String senderEmail = "sender@test.com";
        Integer filmId = 1;
        List<String> targets = List.of("user1@test.com", "user2@test.com");

        when(userRepository.findById(senderEmail)).thenReturn(Optional.of(createMockUser(senderEmail)));
        when(userRepository.findById("user1@test.com")).thenReturn(Optional.of(createMockUser("user1@test.com")));
        when(userRepository.findById("user2@test.com")).thenReturn(Optional.of(createMockUser("user2@test.com")));
        when(filmRepository.findById(filmId)).thenReturn(Optional.of(createMockFilm(filmId)));

        recommendService.sendMultipleRecommendations(senderEmail, filmId, targets);

        verify(recommendRepository, times(2)).save(any(Recommend.class));
    }

    @Test
    void getRecentRecipients_CallsRepositoryWithPageRequest() {
        String email = "sender@test.com";
        
        recommendService.getRecentRecipients(email);

        verify(recommendRepository, times(1)).findLastRecipients(eq(email), any(PageRequest.class));
    }
}