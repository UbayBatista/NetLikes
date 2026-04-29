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

    // --- NUEVA LÓGICA DE DISCOURSE ---

    public Integer getOrCreateForum(Integer filmId, String filmTitle) {
        // 1. Comprobamos si el foro ya existe localmente
        Optional<Forum> existingForum = forumRepository.findById(filmId);

        if (existingForum.isPresent()) {
            // Forum forum =  existingForum.get();
            // Integer forumId = forum.getForumId();
            // User user = userRepository.findById(email).orElseThrow(() -> new RuntimeException("EL Usuario con ID " + email + " no existe en la BD local."));

            // Subscription newSuscription = new Subscription();
            // newSuscription.setForum(forum);
            // newSuscription.setUser(user);
            // this.subscriptionRepository.save(newSuscription);

            // return forumId;

            return existingForum.get().getForumId();
        }

        // 2. Si no existe, lo creamos en el servidor de Discourse
        Integer newTopicId = discourseService.createMovieForum(filmTitle);

        if (newTopicId != null) {
            // 3. Buscamos la película en tu base de datos local
            Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("La película con ID " + filmId + " no existe en la BD local. Guárdala antes de crear el foro."));

            // 4. Creamos y guardamos el nuevo Forum
            Forum newForum = new Forum();
            newForum.setFilm(film);
            newForum.setForumId(newTopicId);
            
            this.forumRepository.save(newForum);

            System.out.println("¡Foro creado/obtenido con éxito! ID:" + filmId + " " + newTopicId);

            // User user = userRepository.findById(email).orElseThrow(() -> new RuntimeException("EL Usuario con ID " + email + " no existe en la BD local."));

            // Subscription newSuscription = new Subscription();
            // newSuscription.setForum(newForum);
            // newSuscription.setUser(user);
            // this.subscriptionRepository.save(newSuscription);
            
            return newTopicId;
        }

        return null;
    }
}