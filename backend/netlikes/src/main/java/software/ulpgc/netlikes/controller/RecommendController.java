package software.ulpgc.netlikes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.ulpgc.netlikes.dto.RecommendCountDTO;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.service.RecommendService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @PostMapping
    public Recommend create(@RequestBody Recommend recommend) {
        return recommendService.addRecommendation(recommend);
    }

    @GetMapping("/user/{email}")
    public List<Recommend> getByUser(@PathVariable String email) {
        return recommendService.getRecommendationsForUser(email);
    }

    @GetMapping("/stats/{email}")
    public List<RecommendCountDTO> getStatsByUser(@PathVariable String email) {
        return recommendService.getRecommendedFilmsWithCount(email);
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> sendMultiple(
            @RequestHeader("X-User-Id") String senderEmail, 
            @RequestBody Map<String, Object> body) {
        
        Integer filmId = (Integer) body.get("filmId");
        @SuppressWarnings("unchecked")
        List<String> targetEmails = (List<String>) body.get("targetEmails");

        recommendService.sendMultipleRecommendations(senderEmail, filmId, targetEmails);
        return ResponseEntity.ok("Recomendaciones enviadas");
    }

    @GetMapping("/recent")
    public ResponseEntity<List<User>> getRecent(@RequestHeader("X-User-Id") String email) {
        return ResponseEntity.ok(recommendService.getRecentRecipients(email));
    }

    @GetMapping("/film/{filmId}/recipients")
    public ResponseEntity<List<String>> getRecipientsForFilm(
            @RequestHeader("X-User-Id") String email, 
            @PathVariable Integer filmId) {
        return ResponseEntity.ok(recommendService.getRecipientsForFilm(email, filmId));
    }
}