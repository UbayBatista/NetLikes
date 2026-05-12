package software.ulpgc.netlikes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public List<Film> getRecommendationsForUser(String userEmail) {
        User user = userRepository.findById(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String userVector = user.getVector();
        if (userVector == null || userVector.isEmpty()) {
            throw new RuntimeException("El usuario aún no tiene gustos configurados");
        }

        return filmRepository.findTop50Recommendations(userEmail, userVector);
    }
}