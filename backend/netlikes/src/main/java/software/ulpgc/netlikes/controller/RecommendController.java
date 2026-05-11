package software.ulpgc.netlikes.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import software.ulpgc.netlikes.dto.RecommendCountDTO;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.service.RecommendService;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
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
}
