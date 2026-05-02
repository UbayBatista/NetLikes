package software.ulpgc.netlikes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.model.MarkId;
import software.ulpgc.netlikes.service.MarkService;
import lombok.RequiredArgsConstructor;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.service.RateService;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.hibernate.annotations.Generated;


@RestController
@RequestMapping("/marks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MarkController {

    private final MarkService markService;
    private final RateService rateService;

    @PostMapping("/{email}/mark/{filmId}")
    public Mark typePelicula(
            @PathVariable String email, 
            @PathVariable Integer filmId, 
            @RequestBody Mark.Type type) {
            
        return markService.typeFilm(email, filmId, type);
    }

    @DeleteMapping("/{email}/mark/{filmId}")
    public void deleteRelation(@PathVariable String email, @PathVariable Integer filmId) {
        markService.deletetype(email, filmId);
    }

    @PostMapping("/{email}/toggle/{filmId}")
    public ResponseEntity<?> toggleMark(
            @PathVariable String email, 
            @PathVariable Integer filmId, 
            @RequestParam("type") String type) {
        
        Mark.Type newType = Mark.Type.valueOf(type.toUpperCase());
        Optional<Mark> currentMark = markService.getMark(email, filmId);

        if (currentMark.isPresent()) {
            if (currentMark.get().getType() == newType) {
                markService.deletetype(email, filmId);
                rateService.deleteRateDirectly(email, filmId); 
                
                return ResponseEntity.ok("{\"status\": \"removed\"}");
            } else {
                markService.typeFilm(email, filmId, newType);
                if (newType == Mark.Type.WATCHLATER) {
                    rateService.deleteRateDirectly(email, filmId);
                }
                
                return ResponseEntity.ok("{\"status\": \"updated\"}");
            }
        } else {
            markService.typeFilm(email, filmId, newType);
            return ResponseEntity.ok("{\"status\": \"added\"}");
        }
    }

    @GetMapping("/{email}/status/{filmId}")
    public ResponseEntity<?> getMarkStatus(@PathVariable String email, @PathVariable Integer filmId) {
        return markService.getMark(email, filmId)
            .map(mark -> {
                Map<String, Object> response = new HashMap<>();
                response.put("email", email);
                response.put("filmId", filmId);
                response.put("type", mark.getType().toString());
                return ResponseEntity.ok((Object) response);
            })
            .orElseGet(() -> {
                Map<String, Object> noMark = new HashMap<>();
                noMark.put("type", "NONE");
                return ResponseEntity.ok(noMark);
            });
    }

    @GetMapping("/{email}/films")
    public ResponseEntity<List<FilmResponseDTO>> getMarkedFilms(
            @PathVariable String email, 
            @RequestParam Mark.Type type) {
        return ResponseEntity.ok(markService.getFilmsByMarkType(email, type));
    }
}