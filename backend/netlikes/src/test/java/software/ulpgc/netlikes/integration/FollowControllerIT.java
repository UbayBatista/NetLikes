package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    properties = {"spring.profiles.active=test"}
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FollowControllerIT {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;

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
        paco.setVector("");
        userRepository.save(paco);

        elena = new User();
        elena.setEmail("elena@gmail.com");
        elena.setName("Elena");
        elena.setAccountPrivacity(true);
        elena.setBirthdate(dummyDate);
        elena.setPassword("123456");
        elena.setSecurityQuestion("¿Color?");
        elena.setAnswer("Azul");
        elena.setVector("");
        userRepository.save(elena);
    }

    @Test
    @DisplayName("Should return Ok and PENDING state when requesting to follow a private account")
    void should_ReturnOkAndPendingState_when_RequestingFollow() throws Exception {
        mockMvc.perform(post("/follows/elena@gmail.com")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    @DisplayName("Should return Ok and accepted state when accepting follow request")
    void should_ReturnOkAndAcceptedState_when_AcceptingFollow() throws Exception {
        Follow pendingRequest = new Follow(paco, elena, Follow.State.PENDING);
        followRepository.save(pendingRequest);

        mockMvc.perform(post("/follows/paco@gmail.com/accept")
                .header("X-User-Id", "elena@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));
    }

    @Test
    @DisplayName("Should return No Content when rejecting a follow request")
    void should_ReturnNoContent_when_RejectingFollow() throws Exception {
        Follow pendingRequest = new Follow(paco, elena, Follow.State.PENDING);
        followRepository.save(pendingRequest);

        mockMvc.perform(delete("/follows/paco@gmail.com/reject")
                .header("X-User-Id", "elena@gmail.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return No Content when unfollowing a user")
    void should_ReturnNoContent_when_UnfollowingUser() throws Exception {
        Follow acceptedFollow = new Follow(paco, elena, Follow.State.ACCEPTED);
        followRepository.save(acceptedFollow);

        mockMvc.perform(delete("/follows/elena@gmail.com/unfollow")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return Ok and accepted state when requesting follow for public account")
    void should_ReturnOkAndAcceptedState_when_RequestingFollowForPublicAccount() throws Exception {
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
    @DisplayName("Should return Ok and list of pending requests when getting pending requests")
    void should_ReturnOkAndList_when_GettingPendingRequests() throws Exception {
        Follow pendingRequest = new Follow(paco, elena, Follow.State.PENDING);
        followRepository.save(pendingRequest);

        mockMvc.perform(get("/follows/pending")
                .header("X-User-Id", "elena@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("paco@gmail.com"));
    }

    @Test
    @DisplayName("Should return Ok when blocking a user")
    void should_ReturnOk_when_BlockingUser() throws Exception {
        mockMvc.perform(post("/follows/elena@gmail.com/block")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return No Content when unblocking a user")
    void should_ReturnNoContent_when_UnblockingUser() throws Exception {
        Follow blockedFollow = new Follow(paco, elena, Follow.State.BLOCKED);
        followRepository.save(blockedFollow);

        mockMvc.perform(post("/follows/elena@gmail.com/unblock")
                .header("X-User-Id", "paco@gmail.com"))
                .andExpect(status().isNoContent());
    }
}