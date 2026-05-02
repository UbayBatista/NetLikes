package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.service.ForumService;
import software.ulpgc.netlikes.model.Forum;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum")
@CrossOrigin(origins = "http://localhost:4200")
public class ForumController {
    
    private final ForumService forumService;

    public ForumController(ForumService forumService){
        this.forumService = forumService;
    }

    @GetMapping  
    public List<Forum> getAll(){
        return this.forumService.getAllForums();
    }

    @PostMapping("/film/{filmId}")
    public ResponseEntity<Forum> create(@PathVariable Integer filmId){
        try {
            Forum createdForum = this.forumService.createForumForFilmId(filmId);
            return ResponseEntity.ok(createdForum);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("FilmNotFound")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        this.forumService.deleteForum(id);
    }
}