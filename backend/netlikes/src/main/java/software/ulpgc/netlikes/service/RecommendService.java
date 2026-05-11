package software.ulpgc.netlikes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.dto.RecommendCountDTO;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.RecommendRepository;
import java.util.List;

@Service
public class RecommendService {
    @Autowired
    private RecommendRepository recommendRepository;

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
}
