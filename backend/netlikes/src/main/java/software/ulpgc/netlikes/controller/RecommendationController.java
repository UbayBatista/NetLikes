package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<Film>> getRecommendations(@RequestHeader("X-User-Id") String email) {
        List<Film> recommendations = recommendationService.getRecommendationsForUser(email);
        return ResponseEntity.ok(recommendations);
    }
}
