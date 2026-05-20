package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.dto.FilmResponseDTO;
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
    public ResponseEntity<List<FilmResponseDTO>> getRecommendations(@RequestHeader("X-User-Id") String email) {
        List<FilmResponseDTO> recommendations = recommendationService.getRecommendationsForUser(email);
        return ResponseEntity.ok(recommendations);
    }
}
