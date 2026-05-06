package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.DiscourseService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test",
        "discourse.api.key=dummy-key",
        "discourse.api.username=dummy-user",
        "discourse.api.url=http://dummy-url.com",
        "discourse.sso.secret=dummy-secret"
    }
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

    @MockitoBean 
    private DiscourseService discourseService;

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

    @Test
    void testBlockUser_ReturnsOk() throws Exception {
        mockMvc.perform(post("/follows/elena@gmail.com/block")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testUnblockUser_ReturnsNoContent() throws Exception {
        Follow blockedFollow = new Follow(paco, elena, Follow.State.BLOCKED);
        followRepository.save(blockedFollow);

        mockMvc.perform(post("/follows/elena@gmail.com/unblock")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isNoContent());
    }
}