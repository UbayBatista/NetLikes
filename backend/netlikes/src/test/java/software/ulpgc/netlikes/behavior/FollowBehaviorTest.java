package software.ulpgc.netlikes.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean; 
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.config.TestConfig;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.FollowId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.DiscourseService;
import software.ulpgc.netlikes.service.FollowService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Import(TestConfig.class)
public class FollowBehaviorTest {

    @Autowired private FollowService followService;
    @Autowired private FollowRepository followRepository;
    @Autowired private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        java.sql.Date dummyDate = new java.sql.Date(System.currentTimeMillis());

        User paco = new User();
        paco.setEmail("paco@gmail.com");
        paco.setName("Paco");
        paco.setBirthdate(dummyDate);
        paco.setPassword("123456");
        paco.setSecurityQuestion("?");
        paco.setAnswer("!");
        paco.setVector("");
        userRepository.save(paco);

        User elena = new User();
        elena.setEmail("elena@gmail.com");
        elena.setName("Elena");
        elena.setAccountPrivacity(true);
        elena.setBirthdate(dummyDate);
        elena.setPassword("123456");
        elena.setSecurityQuestion("?");
        elena.setAnswer("!");
        elena.setVector("");
        userRepository.save(elena);

        User carlos = new User();
        carlos.setEmail("carlos@gmail.com");
        carlos.setName("Carlos");
        carlos.setAccountPrivacity(false);
        carlos.setBirthdate(dummyDate);
        carlos.setPassword("123456");
        carlos.setSecurityQuestion("?");
        carlos.setAnswer("!");
        carlos.setVector("");
        userRepository.save(carlos);
    }

    @Test
    @DisplayName("Should accept follow request and update status to ACCEPTED")
    void acceptFollowRequest() {
        followService.requestFollow("paco@gmail.com", "elena@gmail.com");

        followService.acceptFollow("paco@gmail.com", "elena@gmail.com");

        Follow follow = followRepository.findById(new FollowId("paco@gmail.com", "elena@gmail.com")).orElseThrow();
        assertEquals(Follow.State.ACCEPTED, follow.getState(), "El solicitante debe pasar a seguir al usuario.");
    }

    @Test
    @DisplayName("Should reject follow request and remove it from database")
    void rejectFollowRequest() {
        followService.requestFollow("paco@gmail.com", "elena@gmail.com");

        followService.rejectFollow("paco@gmail.com", "elena@gmail.com");

        boolean exists = followRepository.existsById(new FollowId("paco@gmail.com", "elena@gmail.com"));
        assertFalse(exists, "El solicitante no debe seguir al usuario y la solicitud debe ser eliminada.");
    }

    @Test
    @DisplayName("Should follow public account without needing acceptance")
    void followPublicAccount_AutoAccepts() {
        followService.requestFollow("paco@gmail.com", "carlos@gmail.com");

        Follow follow = followRepository.findById(new FollowId("paco@gmail.com", "carlos@gmail.com")).orElseThrow();
        assertEquals(Follow.State.ACCEPTED, follow.getState(), "Si la cuenta es pública, pasa a ACCEPTED al instante.");
    }

    @Test
    @DisplayName("Should block user and update status to BLOCKED")
    void blockUser() {
        followService.blockUser("paco@gmail.com", "elena@gmail.com");

        Follow follow = followRepository.findById(new FollowId("paco@gmail.com", "elena@gmail.com")).orElseThrow();
        assertEquals(Follow.State.BLOCKED, follow.getState(), "El estado de la relación debe ser BLOCKED");
        
        String status = followService.checkStatus("elena@gmail.com", "paco@gmail.com");
        assertEquals("BLOCKED", status, "Si Paco bloquea a Elena, a Elena le debe salir estado BLOCKED con respecto a Paco");
    }

    @Test
    @DisplayName("Should unblock user and remove follow record from database")
    void unblockUser() {
        followService.blockUser("paco@gmail.com", "elena@gmail.com");
        
        followService.unblockUser("paco@gmail.com", "elena@gmail.com");

        boolean exists = followRepository.existsById(new FollowId("paco@gmail.com", "elena@gmail.com"));
        assertFalse(exists, "Al desbloquear, el registro de follow debe eliminarse");
    }
}