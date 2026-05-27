package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class FollowRepositoryIT {

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
        user.setVector("");
        
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
    @DisplayName("Should get a follow by follower and followed")
    void should_GetFollowByFollowerAndFollowed_when_FollowExists() {
        User follower = createUser("follower@test.com", "Seguidor");
        User followed = createUser("target@test.com", "Objetivo");
        entityManager.flush();

        Follow follow = this.createFollow(follower, followed, Follow.State.PENDING);
        Follow savedFollow = repository.save(follow);
        entityManager.flush();

        Follow followedList = repository.findByFollowed_Email(followed.getEmail()).get(0);
        assertThat(followedList).isNotNull();
        assertThat(followedList).isEqualTo(savedFollow);
        assertThat(followedList.getState()).isEqualTo(Follow.State.PENDING);

        Follow followerList = repository.findByFollower_Email(follower.getEmail()).get(0);
        assertThat(followerList).isNotNull();
        assertThat(followerList).isEqualTo(savedFollow);
        assertThat(followerList.getState()).isEqualTo(Follow.State.PENDING);
    }
}