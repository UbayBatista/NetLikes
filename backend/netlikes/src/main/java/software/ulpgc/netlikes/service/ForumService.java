package software.ulpgc.netlikes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.Forum;
import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.ForumRepository;
import software.ulpgc.netlikes.repository.SubscriptionRepository;
import software.ulpgc.netlikes.repository.UserRepository;

@Service
public class ForumService {
    
    private final ForumRepository forumRepository;
    private final FilmRepository filmRepository; // Añadido para buscar la película
    private final DiscourseService discourseService; // Añadido para conectar con Discourse
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    // Actualizamos el constructor para inyectar las nuevas dependencias
    public ForumService(ForumRepository forumRepository, FilmRepository filmRepository, 
            DiscourseService discourseService, UserRepository userRepository, SubscriptionRepository subscriptionRepository){
        this.forumRepository = forumRepository;
        this.filmRepository = filmRepository;
        this.discourseService = discourseService;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Forum> getAllForums(){
        return this.forumRepository.findAll();
    }

    public Forum createForum(Forum forum) {
        return this.forumRepository.save(forum);
    }

    public void deleteForum(Integer id){
        this.forumRepository.deleteById(id);
    }

    public Integer getOrCreateForum(Integer filmId, String filmTitle) {
        Optional<Forum> existingForum = forumRepository.findById(filmId);

        if (existingForum.isPresent()) {
            return existingForum.get().getForumId();
        }
        
        Integer newTopicId = null;

        try {
             newTopicId = discourseService.createMovieForum(filmTitle);
        } catch (Exception e) {

            System.out.println("El foro posiblemente ya existe en Discourse. Buscando ID...");           
            newTopicId = discourseService.getForumIdByTitle(filmTitle);
            
            if (newTopicId == null) {
                throw new RuntimeException("No se pudo crear ni encontrar el foro en Discourse.", e);
            }
        }

        if (newTopicId != null) {
            Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("La película con ID " + filmId + " no existe en la BD local. Guárdala antes de crear el foro."));

            Forum newForum = new Forum();
            newForum.setFilm(film);
            newForum.setForumId(newTopicId);
            
            this.forumRepository.save(newForum);

            System.out.println("¡Foro creado/obtenido con éxito! FilmID:" + filmId + "Forum ID: " + newTopicId);            
            return newTopicId;
        }

        return null;
    }
}