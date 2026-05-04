package software.ulpgc.netlikes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.Forum;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.ForumRepository;

@Service
public class ForumService {
    
    private final ForumRepository forumRepository;
    private final DiscourseService discourseService;
    private final FilmRepository filmRepository; 

    public ForumService(ForumRepository forumRepository, DiscourseService discourseService, FilmRepository filmRepository){
        this.forumRepository = forumRepository;
        this.discourseService = discourseService;
        this.filmRepository = filmRepository;
    }

    public List<Forum> getAllForums(){
        return this.forumRepository.findAll();
    }

    @Transactional
     public Integer getOrCreateForum(Integer filmId, String filmTitle) {
        Optional<Forum> existingForum = forumRepository.findById(filmId);

        if (existingForum.isPresent()) {
            return existingForum.get().getDiscourseTopicId();
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
            newForum.setDiscourseTopicId(newTopicId);
            
            this.forumRepository.save(newForum);

            System.out.println("¡Foro creado/obtenido con éxito! FilmID:" + filmId + "Forum ID: " + newTopicId);            
            return newTopicId;
        }
        return null;
    }

    @Transactional
    public void deleteForum(Integer id){
        Optional<Forum> forumOpt = forumRepository.findById(id);
        
        if(forumOpt.isPresent()) {
            Forum forum = forumOpt.get();
            
            discourseService.deleteTopic(forum.getDiscourseTopicId(), forum.getFilm().getTitle());
            
            Film film = forum.getFilm();
            if (film != null) {
                film.setForum(null);
                filmRepository.saveAndFlush(film);
            }
            
            this.forumRepository.delete(forum);
            this.forumRepository.flush();
        }
    }
}