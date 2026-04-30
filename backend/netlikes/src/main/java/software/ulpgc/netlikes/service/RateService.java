package software.ulpgc.netlikes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import software.ulpgc.netlikes.model.Rate;
import software.ulpgc.netlikes.model.RateId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.repository.RateRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.repository.FilmRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

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
                return rateRepository.save(existing);
            }
        } 
        else {
            User user = userRepository.findById(email).orElseThrow();
            Film film = filmRepository.findById(filmId).orElseThrow();

            Rate rate = new Rate();
            rate.setId(id);
            rate.setUser(user);
            rate.setFilm(film);
            rate.setScore(score);
            return rateRepository.save(rate);
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