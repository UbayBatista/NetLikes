package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.ForumRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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
        entityManager.persist(film);
        return film;
    }

    private Forum createForum(Film film) {
        Forum forum = new Forum();
        forum.setFilm(film); 
        forum.setForumId(film.getId() + 1234); 
        entityManager.persist(forum);
        return forum;
    }

    @Test
    @DisplayName("Debe guardar un foro para una película creada")
    void shouldSaveForum() {
        Film film = this.createFilm(500);
        Forum forum = repository.save(this.createForum(film));

        entityManager.flush();

        assertThat(forum).isNotNull();
        assertThat(forum.getId()).isEqualTo(film.getId());
        assertThat(repository.findAll().get(0)).isEqualTo(forum);
    }

    @Test
    @DisplayName("Debe guardar un foro para cada película creada")
    void shouldSaveMultipleForums() {
        List<Film> films = Stream.of(501, 502, 503, 504, 505)
                            .map(i -> this.createFilm(i)).toList();

        entityManager.flush();

        films.stream().forEach(f -> {
                            entityManager.flush();
                            repository.save(this.createForum(f));
        });

        assertThat(!repository.findAll().isEmpty()).isTrue();
        assertThat(repository.findAll().size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe eliminar un foro creado")
    void shouldDeleteForum() {
        Film film = this.createFilm(500);
        Forum forum = this.repository.save(this.createForum(film));

        this.repository.delete(forum);

        entityManager.flush();

        assertThat(repository.findAll()).isEmpty();
        assertThat(repository.findById(forum.getId())).isEmpty();
    }

    @Test
    @DisplayName("Debe eliminar un único foro de varios creados")
    void shouldDeleteOneForum() {
        List<Film> films = Stream.of(501, 502, 503, 504, 505)
                            .map(i -> this.createFilm(i)).toList();

        entityManager.flush();

        List<Forum> forums = films.stream().map(f -> {
                            entityManager.flush();
                            return repository.save(this.createForum(f));
        }).toList();

        entityManager.flush();

        repository.delete(forums.get(0));

        assertThat(repository.findById(forums.get(0).getId())).isEmpty();
        assertThat(repository.findAll().size()).isEqualTo(4);
    }

    @Test
    void shouldRemoveForumAfterFilmIsRemoved() {
        Film film = this.createFilm(500);
        Forum forum = this.createForum(film);
        
        film.setForum(forum);

        repository.save(forum);

        entityManager.flush();
        entityManager.clear();

        Film filmToDelete = entityManager.find(Film.class, film.getId());

        if (filmToDelete != null) entityManager.remove(filmToDelete);

        assertThat(repository.findAll()).isEmpty();
        assertThat(repository.findById(forum.getId())).isEmpty();
    }
}
