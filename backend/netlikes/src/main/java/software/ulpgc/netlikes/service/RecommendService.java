package software.ulpgc.netlikes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.dto.RecommendCountDTO;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.RecommendRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.repository.FilmRepository;

import java.util.List;

@Service
public class RecommendService {
    @Autowired
    private RecommendRepository recommendRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FilmRepository filmRepository;

    public Recommend addRecommendation(Recommend recommend) {
        recommend.setDate(new java.sql.Date(System.currentTimeMillis()));
        return recommendRepository.save(recommend);
    }

    public List<Recommend> getRecommendationsForUser(String email) {
        return recommendRepository.findByRecommendedEmail(email);
    }

    public List<RecommendCountDTO> getRecommendedFilmsWithCount(String email) {
        return recommendRepository.countRecommendationsByUser(email);
    }

    public List<User> getRecentRecipients(String email) {
        return recommendRepository.findLastRecipients(email, PageRequest.of(0, 10));
    }

    @Transactional
    public void sendMultipleRecommendations(String recommenderEmail, Integer filmId, List<String> recommendedEmails) {
        User recommender = userRepository.findById(recommenderEmail).orElseThrow();
        Film film = filmRepository.findById(filmId).orElseThrow();

        for (String targetEmail : recommendedEmails) {
            User recommended = userRepository.findById(targetEmail).orElseThrow();
            
            Recommend rec = new Recommend();
            rec.setRecommender(recommender);
            rec.setRecommended(recommended);
            rec.setFilm(film);
            rec.setDate(new java.sql.Date(System.currentTimeMillis()));
            
            recommendRepository.save(rec);
        }
    }

    public List<String> getRecipientsForFilm(String email, Integer filmId) {
        return recommendRepository.findRecipientsByFilm(email, filmId);
    }
}