package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class FollowRepositoryTest {

    @Autowired 
    private FollowRepository repository;
    
    @Autowired 
    private EntityManager entityManager;
    
    private User createUser(String userEmail, String userName) {
        User user = new User();
        user.setEmail(userEmail);
        user.setPassword("1234");
        user.setSecurityQuestion("¿?");
        user.setAnswer("!");
        user.setName(userName);
        user.setBirthdate(new Date());
        user.setAccountPrivacity(false);
        user.setProfilePicture("/");
        
        entityManager.persist(user);
        return user;
    }

    private Follow createFollow(User follower, User followed, Follow.State state) {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setState(state);
        return follow;
    }

    @Test
    @DisplayName("Should save follow with PENDING state")
    void shouldSaveFollow() {
        User follower = this.createUser("follower@test.com", "Seguidor");
        User followed = this.createUser("target@test.com", "Objetivo");
        entityManager.flush(); 

        Follow follow = this.createFollow(follower, followed, Follow.State.PENDING);
        Follow savedFollow = repository.save(follow);
        entityManager.flush(); 

        assertThat(savedFollow).isNotNull();
        assertThat(savedFollow.getState()).isEqualTo(Follow.State.PENDING);
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
    }

    @Test
    @DisplayName("Should delete follow")
    void shouldRemoveFollow() {
        User follower = this.createUser("follower@test.com", "Seguidor");
        User followed = this.createUser("target@test.com", "Objetivo");
        
        Follow follow = repository.save(this.createFollow(follower, followed, Follow.State.ACCEPTED));
        entityManager.flush();

        repository.delete(follow);
        entityManager.flush();

        assertThat(repository.findAll()).isEmpty();
    }
}