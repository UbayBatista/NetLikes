package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.service.ForumService;
import software.ulpgc.netlikes.service.SubscriptionService;
import software.ulpgc.netlikes.model.Forum;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum")
public class ForumController {
    
    
    private final ForumService forumService;
    private final SubscriptionService subscriptionService;

    public ForumController(ForumService forumService, SubscriptionService subscriptionService){
        this.forumService = forumService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping  
    public List<Forum> getAll(){
        return this.forumService.getAllForums();
    }

    // @PostMapping("/film/{filmId}")
    // public ResponseEntity<Forum> create(@PathVariable Integer filmId){
    //     try {
    //         Forum createdForum = this.forumService.createForumForFilmId(filmId);
    //         return ResponseEntity.ok(createdForum);
    //     } catch (RuntimeException e) {
    //         if (e.getMessage().equals("FilmNotFound")) {
    //             return ResponseEntity.notFound().build();
    //         }
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }

    @PostMapping("/film/{filmId}")
    public ResponseEntity<?> subscribeToFilmForum(@PathVariable Integer filmId, @RequestBody Map<String, String> payload) {
        String filmTitle = payload.get("title");
        String email = payload.get("email");
        
        if (filmTitle == null || filmTitle.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El título de la película es obligatorio"));
        }

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El email del usuario es obligatorio"));
        }

        try {
            Integer topicId = this.forumService.getOrCreateForum(filmId, filmTitle);
            
            if (topicId != null) {
                this.subscriptionService.subscribeUserToFilm(email, filmId);
                return ResponseEntity.ok(Map.of("discourseTopicId", topicId));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "Error al conectar con Discourse"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        this.forumService.deleteForum(id);
    }
}