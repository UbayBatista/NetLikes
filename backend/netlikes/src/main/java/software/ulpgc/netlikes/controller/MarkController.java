package software.ulpgc.netlikes.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.service.MarkService;
import software.ulpgc.netlikes.service.RateService;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marks")
@RequiredArgsConstructor
public class MarkController {

    private final MarkService markService;
    private final RateService rateService;


    @PostMapping("/{email}/toggle/{filmId}")
    public ResponseEntity<?> toggleMark(
            @PathVariable String email, 
            @PathVariable Integer filmId, 
            @RequestParam("type") String type) {
        
        Mark.Type newType = Mark.Type.valueOf(type.toUpperCase());
        
        String result = markService.toggleMarkLogic(email, filmId, newType);

        if ((result.equals("added") && newType == Mark.Type.WATCHLATER) || 
            (result.equals("removed") && newType == Mark.Type.SEEN)) {
            rateService.deleteRateDirectly(email, filmId); 
        }
        
        return ResponseEntity.ok("{\"status\": \"" + result + "\"}");
    }

    @GetMapping("/{email}/status/{filmId}")
    public ResponseEntity<?> getMarkStatus(@PathVariable String email, @PathVariable Integer filmId) {
        List<Mark.Type> types = markService.getMarkTypesForFilm(email, filmId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("filmId", filmId);
        response.put("types", types); 
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{email}/films")
    public ResponseEntity<List<FilmResponseDTO>> getMarkedFilms(
            @PathVariable String email, 
            @RequestParam Mark.Type type) {
        
        return ResponseEntity.ok(markService.getFilmsByMarkType(email, type));
    }
}