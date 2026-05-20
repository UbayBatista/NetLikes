package software.ulpgc.netlikes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.ulpgc.netlikes.model.Rate;
import software.ulpgc.netlikes.model.RateId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.RateRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.repository.FilmRepository;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Rate toggleRate(String email, Integer filmId, Rate.Score score) {
        RateId id = new RateId(email, filmId);
        Optional<Rate> existingOpt = rateRepository.findById(id);

        if (existingOpt.isPresent()) {
            Rate existing = existingOpt.get();
            
            if (existing.getScore() == score) {
                rateRepository.delete(existing);
                return null; 
            } 
            else {
                existing.setScore(score);
                updateUserVector(existing.getUser(), existing.getFilm(), score);
                return rateRepository.save(existing);
            }
        } 
        else {
            User user = userRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Película no encontrada"));

            Rate rate = new Rate();
            rate.setId(id);
            rate.setUser(user);
            rate.setFilm(film);
            rate.setScore(score);
            
            updateUserVector(user, film, score);
            
            return rateRepository.save(rate);
        }
    }

    private void updateUserVector(User user, Film film, Rate.Score score) {
        try {
            if (user.getVector() == null || film.getVector() == null) {
                return;
            }

            double filmWeight = 0.0;
            if (score == Rate.Score.LOVE) {
                filmWeight = 0.30;
            } else if (score == Rate.Score.LIKE) {
                filmWeight = 0.15;
            } else if (score == Rate.Score.DISLIKE) {
                filmWeight = -0.15;
            }
            
            double userWeight = 1.0 - Math.abs(filmWeight);

            double[] userVector = objectMapper.readValue(user.getVector(), double[].class);
            double[] filmVector = objectMapper.readValue(film.getVector(), double[].class);

            for (int i = 0; i < userVector.length; i++) {
                userVector[i] = (userVector[i] * userWeight) + (filmVector[i] * filmWeight);
            }

            user.setVector(objectMapper.writeValueAsString(userVector));
            userRepository.save(user);
            
            log.info("Vector recalculado para {}. Score: {}. Desplazamiento: {}%", 
                     user.getEmail(), score, filmWeight * 100);

        } catch (Exception e) {
            log.error("Error matemático al aplicar valoración al vector del usuario", e);
        }
    }

    public Optional<Rate> getRate(String email, Integer filmId) {
        return rateRepository.findById(new RateId(email, filmId));
    }

    @Transactional
    public void deleteRateDirectly(String email, Integer filmId) {
        rateRepository.deleteById(new RateId(email, filmId));
    }
}