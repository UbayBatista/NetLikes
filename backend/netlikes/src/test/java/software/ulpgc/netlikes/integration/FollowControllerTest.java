package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    private User paco;
    private User elena;

    @BeforeEach
    void setUp() {
        java.sql.Date dummyDate = new java.sql.Date(System.currentTimeMillis());
        
        paco = new User();
        paco.setEmail("paco@gmail.com");
        paco.setName("Paco");
        paco.setBirthdate(dummyDate);
        paco.setPassword("123456"); 
        paco.setSecurityQuestion("¿Color?");
        paco.setAnswer("Rojo");
        userRepository.save(paco);

        elena = new User();
        elena.setEmail("elena@gmail.com");
        elena.setName("Elena");
        elena.setAccountPrivacity(true);
        elena.setBirthdate(dummyDate);
        elena.setPassword("123456");
        elena.setSecurityQuestion("¿Color?");
        elena.setAnswer("Azul");
        userRepository.save(elena);
    }

    @Test
    void testRequestFollow_ReturnsOkAndPendingState() throws Exception {
        mockMvc.perform(post("/follows/elena@gmail.com")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void testAcceptFollow_ReturnsOkAndAcceptedState() throws Exception {
        // Usamos los objetos paco y elena creados en setUp
        Follow pendingRequest = new Follow(paco, elena, Follow.State.PENDING);
        followRepository.save(pendingRequest);

        mockMvc.perform(post("/follows/paco@gmail.com/accept")
                .header("X-User-Id", "elena@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));
    }

    @Test
    void testRejectFollow_ReturnsNoContent() throws Exception {
        Follow pendingRequest = new Follow(paco, elena, Follow.State.PENDING);
        followRepository.save(pendingRequest);

        mockMvc.perform(delete("/follows/paco@gmail.com/reject")
                .header("X-User-Id", "elena@gmail.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUnfollow_ReturnsNoContent() throws Exception {
        Follow acceptedFollow = new Follow(paco, elena, Follow.State.ACCEPTED);
        followRepository.save(acceptedFollow);

        mockMvc.perform(delete("/follows/elena@gmail.com/unfollow")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testRequestFollow_PublicAccount_ReturnsOkAndAcceptedState() throws Exception {
        java.sql.Date dummyDate = new java.sql.Date(System.currentTimeMillis());
        User publico = new User();
        publico.setEmail("publico@gmail.com");
        publico.setName("Publico");
        publico.setBirthdate(dummyDate);
        publico.setPassword("123456");
        publico.setSecurityQuestion("?");
        publico.setAnswer("!");
        publico.setAccountPrivacity(false);
        userRepository.save(publico);

        mockMvc.perform(post("/follows/publico@gmail.com")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));
    }

    @Test
    void testGetPendingRequests_ReturnsOkAndList() throws Exception {
        Follow pendingRequest = new Follow(paco, elena, Follow.State.PENDING);
        followRepository.save(pendingRequest);

        mockMvc.perform(get("/follows/pending")
                .header("X-User-Id", "elena@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("paco@gmail.com"));
    }
}

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
        // Accedemos al email a través del objeto User
        assertThat(savedFollow.getFollower().getEmail()).isEqualTo("follower@test.com");
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

    @Test
    @DisplayName("Should delete follow")
    void shouldRemoveFollow() {
        User follower = this.createUser("follower@test.com", "Seguidor");
        User followed = this.createUser("target@test.com", "Objetivo");
        
        Follow follow = repository.save(this.createFollow(follower, followed, Follow.State.ACCEPTED));
        entityManager.flush();

        assertThat(repository.findAll()).isNotEmpty();

        repository.delete(follow);
        entityManager.flush();

        assertThat(repository.findAll()).isEmpty();
    }
}