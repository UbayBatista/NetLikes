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
    public Forum createForumForFilmId(Integer filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("FilmNotFound"));

        Optional<Forum> existingForum = forumRepository.findById(filmId);
        if (existingForum.isPresent()) {
            return existingForum.get();
        }

        Integer topicId = discourseService.createMovieTopic(film.getTitle(), film.getId());

        if (topicId != null) {
            Forum forum = new Forum();
            forum.setFilm(film);
            forum.setDiscourseTopicId(topicId);
            return this.forumRepository.save(forum);
        } else {
            throw new RuntimeException("DiscourseError");
        }
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