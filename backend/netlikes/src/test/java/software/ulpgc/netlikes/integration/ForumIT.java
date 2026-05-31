package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.ForumRepository;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ForumRepositoryIntegrationTest {

    @Autowired private ForumRepository repository;
    @Autowired private EntityManager entityManager;

    private Film createFilm(Integer id) {
        Film film = new Film();
        film.setId(id);
        film.setOverView("Esta película es una maravilla");
        film.setTitle("Esta abuela es un peligro");
        film.setAdult(false);
        film.setPosterPath("poster/path");
        film.setVector("[0.0, 0.0, 0.0]");
        entityManager.persist(film);
        return film;
    }

    private Forum createForum(Film film) {
        Forum forum = new Forum();
        forum.setFilm(film); 
        forum.setDiscourseTopicId(film.getId() + 1234);
        entityManager.persist(forum);
        return forum;
    }

    @Test
    @DisplayName("Should remove forum automatically (Cascade) when film is removed")
    void should_RemoveForum_when_FilmIsRemoved() {
        Film film = createFilm(500);
        Forum forum = createForum(film);

        film.setForum(forum);
        repository.save(forum);
        
        entityManager.flush();
        entityManager.clear();
        
        Film filmToDelete = entityManager.find(Film.class, film.getId());
        if (filmToDelete != null) {
            entityManager.remove(filmToDelete);
        }
        
        entityManager.flush();
        
        assertThat(repository.findAll()).isEmpty();
        assertThat(repository.findById(forum.getId())).isEmpty();
    }
}