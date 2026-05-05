package software.ulpgc.netlikes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import software.ulpgc.netlikes.model.Rate;
import software.ulpgc.netlikes.service.RateService;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class RateController {
    private final RateService rateService;

    @PostMapping("/{email}/{filmId}")
    public ResponseEntity<?> rateFilm(
            @PathVariable String email, 
            @PathVariable Integer filmId, 
            @RequestParam Rate.Score score) {
        
        Rate result = rateService.toggleRate(email, filmId, score);
        
        if (result == null) {
            return ResponseEntity.ok(Collections.singletonMap("status", "removed"));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{email}/{filmId}")
    public ResponseEntity<?> getRateStatus(@PathVariable String email, @PathVariable Integer filmId) {
        Optional<Rate> rateOpt = rateService.getRate(email, filmId);
        
        if (rateOpt.isPresent()) {
            return ResponseEntity.ok(rateOpt.get());
        } else {
            return ResponseEntity.ok(Collections.singletonMap("score", "NONE"));
        }
    }
}