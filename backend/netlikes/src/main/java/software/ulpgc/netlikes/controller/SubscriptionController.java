package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.service.SubscriptionService;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscribe_to")
@CrossOrigin(origins = "http://localhost:4200")
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService){
        this.subscriptionService = subscriptionService;
    }

    @GetMapping  
    public List<Subscription> getAll(){
        return this.subscriptionService.getAllSubscriptions();
    }

    @GetMapping("/{email:.+}")
    public ResponseEntity<?> getByUserId(@PathVariable String email) {
        System.out.println("---- PETICIÓN RECIBIDA EN BACKEND ----");
        System.out.println("Buscando foros para el email EXACTO: " + email);
        
        try {
            List<Subscription> subs = this.subscriptionService.getByUserId(email);
            System.out.println("Suscripciones encontradas en la BD: " + subs.size());
            
            
            List<java.util.Map<String, Object>> responseList = subs.stream().map(sub -> {
                java.util.Map<String, Object> subMap = new java.util.HashMap<>();
                
                
                java.util.Map<String, Object> idMap = new java.util.HashMap<>();
                idMap.put("email", sub.getId().getEmail());
                idMap.put("forumId", sub.getId().getForumId());
                subMap.put("id", idMap);
                
                
                java.util.Map<String, Object> forumMap = new java.util.HashMap<>();
                forumMap.put("id", sub.getForum().getId());
                forumMap.put("discourseTopicId", sub.getForum().getDiscourseTopicId());
                
                
                java.util.Map<String, Object> filmMap = new java.util.HashMap<>();
                filmMap.put("id", sub.getForum().getFilm().getId()); 
                filmMap.put("title", sub.getForum().getFilm().getTitle());
                
                forumMap.put("film", filmMap);
                subMap.put("forum", forumMap);
                
                return subMap;
            }).toList();
            
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/{email}/film/{filmId}")
    public ResponseEntity<?> subscribe(@PathVariable String email, @PathVariable Integer filmId){
        try {
            Subscription subscription = this.subscriptionService.subscribeUserToFilm(email, filmId);
            return ResponseEntity.ok(subscription);
        } catch (RuntimeException e) {
            System.err.println("Error en la suscripción: " + e.getMessage()); 
            
            if (e.getMessage().equals("UserNotFound")) {
                return ResponseEntity.status(404).body("Usuario no encontrado en la base de datos local.");
            } else if (e.getMessage().equals("FilmNotFound")) {
                return ResponseEntity.status(404).body("La película debe estar guardada en tu base de datos (márcala como vista o favoritos primero).");
            }
            return ResponseEntity.status(500).body("Error al conectar con Discourse: " + e.getMessage());
        }
    }

    @DeleteMapping("/{email}/unsubscribe/{forumId}")
    public ResponseEntity<?> unsubscribe(@PathVariable String email, @PathVariable Integer forumId) {
        try {
            this.subscriptionService.deleteSubscription(email, forumId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}