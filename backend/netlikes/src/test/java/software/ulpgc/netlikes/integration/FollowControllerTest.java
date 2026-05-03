package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import jakarta.persistence.EntityManager;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.FollowRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FollowRepositoryIntegrationTest {

    @Autowired 
    private FollowRepository repository;
    
    @Autowired 
    private EntityManager entityManager;
    
    private User createUser(String userEmail, String userName) {
        User user = new User();
        user.setEmail(userEmail);
        user.setPassword("1234");
        user.setSecurityQuestion("¿Tienes marca de nacimiento?");
        user.setAnswer("Sí");
        user.setName(userName);
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

    private Follow createFollow(User follower, User followed, Follow.State state) {
        Follow follow = new Follow();
        follow.setFollowerId(follower.getEmail());
        follow.setFollowedId(followed.getEmail());
        follow.setState(state);
        
        return follow;
    }

    @Test
    @DisplayName("Shoudl save follow with PENDING state")
    void shouldSaveFollow() {

        User follower = this.createUser("follower@test.com", "Seguidor");
        User followed = this.createUser("target@test.com", "Objetivo");
        entityManager.flush(); 

        Follow follow = this.createFollow(follower, followed, Follow.State.PENDING);

        Follow savedFollow = repository.save(follow);
        entityManager.flush(); 

        assertThat(savedFollow).isNotNull();
        assertThat(savedFollow.getFollowerId()).isEqualTo("follower@test.com");
        assertThat(savedFollow.getState()).isEqualTo(Follow.State.PENDING);

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().get(0)).isEqualTo(savedFollow);
    }

    @Test
    @DisplayName("Should update state from PENDING to ACCEPTED")
    void shouldUpdateFollowState() {
        User follower = this.createUser("follower@test.com", "Seguidor");
        User followed = this.createUser("target@test.com", "Objetivo");
        
        Follow initialFollow = repository.save(this.createFollow(follower, followed, Follow.State.PENDING));
        entityManager.flush();

        initialFollow.setState(Follow.State.ACCEPTED);
        Follow updatedFollow = repository.save(initialFollow);
        entityManager.flush();

        assertThat(updatedFollow.getState()).isEqualTo(Follow.State.ACCEPTED);
        assertThat(repository.findAll()).hasSize(1); 
    }
}