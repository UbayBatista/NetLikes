package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.service.ForumService;
import software.ulpgc.netlikes.model.Forum;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/forum")
@CrossOrigin(origins = "http://localhost:4200") // Asegúrate de tener esto para Angular
public class ForumController {
    
    private final ForumService forumService;

    public ForumController(ForumService forumService){
        this.forumService = forumService;
    }

    @GetMapping  
    public List<Forum> getAll(){
        return this.forumService.getAllForums();
    }

    @PostMapping
    public Forum create(@Valid @RequestBody Forum forum){
        return this.forumService.createForum(forum);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        this.forumService.deleteForum(id);
    }

    // --- NUEVO ENDPOINT PARA ANGULAR ---

    @PostMapping("/film/{filmId}")
    public ResponseEntity<?> subscribeToFilmForum(@PathVariable Integer filmId, @RequestBody Map<String, String> payload) {
        String filmTitle = payload.get("title");
        
        if (filmTitle == null || filmTitle.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El título de la película es obligatorio"));
        }

        Integer topicId = this.forumService.getOrCreateForum(filmId, filmTitle);
        
        if (topicId != null) {
            return ResponseEntity.ok(Map.of("discourseTopicId", topicId));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al conectar con Discourse"));
        }
    }
}