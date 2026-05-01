package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.SubscriptionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SubscriptionRepositoryIntegrationTest {
    @Autowired private SubscriptionRepository repository;
    
    @Autowired private EntityManager entityManager;
    
    private User createUser(String userEmail) {
        User user = new User();
        user.setEmail(userEmail);
        user.setPassword("1234");
        user.setSecurityQuestion("¿Tienes marca de nacimiento?");
        user.setAnswer("Sí");
        user.setName("UsuerioDePrueba");
        user.setBirthdate(new Date());
        user.setAccountPrivacity(false);
        user.setShowWatchedFilms(false);
        user.setShowFilmsToWatchLater(false);
        user.setShowRecommendedFilms(false);
        user.setProfilePicture("/");
        user.setBio("Holaaa, soy una prueba.");
        entityManager.persist(user);
        return user;
    }

    private Film createFilm() {
        Film film = new Film();
        film.setId(500);
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
        forum.setForumId("123456"); 
        entityManager.persist(forum);
        return forum;
    }

    private Subscription createSub(User user, Forum forum) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setForum(forum);
        sub.setId(new SubscriptionId(user.getEmail(), forum.getId()));
        return sub;
    }

    private List<Subscription> prepareSub(Stream<String> usersEmail) {
        Film film = this.createFilm();
        Forum forum = this.createForum(film);
        List<Subscription> subs = usersEmail.map(user -> {
            User userCreated = this.createUser(user);
            entityManager.flush();
            Subscription savedSub = repository.save(this.createSub(userCreated, forum));
            return savedSub;
        })
        .toList();

        entityManager.flush();

        return subs;
    }


    @Test
    @DisplayName("Debe guardar una suscripción con sus dependencias en una BD real")
    void shouldSaveSubscription() {
        Subscription subscription = this.prepareSub(Stream.of("usuario@test.com")).get(0);

        assertThat(subscription).isNotNull();
        assertThat(subscription.getId().getEmail()).isEqualTo("usuario@test.com");
        assertThat(!(repository.getByUserEmail(subscription.getId().getEmail())).isEmpty()).isTrue();
        assertThat(repository.findAll().get(0)).isEqualTo(subscription);
    }

    @Test
    @DisplayName("Debe eliminar la subscripción indicada a un foro")
    void shouldRemoveSubscription() {
        List<Subscription> subscriptions = this.prepareSub(Stream.of("usuario@test.com", "usuario2@test.com", "usuario3@test.com"));

        repository.delete(subscriptions.get(0));

        assertThat(repository.findAll().isEmpty()).isFalse();
        assertThat(repository.findAll()).isNotEqualTo(subscriptions);
        List<Subscription> subs = subscriptions.subList(1, 3);
        assertThat(repository.findAll()).isEqualTo(subs);
    }

    @Test
    @DisplayName("Debe eliminar la última subscripción a un foro")
    void shouldRemoveLastSubscription() {
        Subscription subscription = this.prepareSub(Stream.of("usuario@test.com")).get(0);

        repository.delete(subscription);

        assertThat(repository.findAll()).isEmpty();
    }   
}