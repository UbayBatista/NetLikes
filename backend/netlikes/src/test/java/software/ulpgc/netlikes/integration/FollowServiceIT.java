package software.ulpgc.netlikes.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import software.ulpgc.netlikes.model.Notify;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.NotifyRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.FollowService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
public class FollowServiceIT {

    @Autowired private FollowService followService;
    @Autowired private NotifyRepository notifyRepository;
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
    @DisplayName("Should receive notification on follow request")
    void should_ReceiveNotification_when_UserRequestsToFollow() {
        followService.requestFollow("paco@gmail.com", "elena@gmail.com");

        List<Notify> notifications = notifyRepository.findByUserReceiverEmailOrderByDateDesc("elena@gmail.com");
        assertEquals(1, notifications.size(), "Debe existir una notificación en el buzón");
        assertEquals(Notify.Type.FOLLOWREQUEST, notifications.get(0).getType());
        assertEquals("Paco quiere seguirte.", notifications.get(0).getId().getMessage());
        assertFalse(notifications.get(0).isRead(), "La notificación debe estar pendiente (no leída)");
    }

    @Test
    @DisplayName("Should remove notification when follow request is cancelled")
    void should_RemoveNotification_when_FollowRequestIsCancelled() {
        followService.requestFollow("paco@gmail.com", "elena@gmail.com");

        followService.deleteFollow("paco@gmail.com", "elena@gmail.com");

        List<Notify> notifications = notifyRepository.findByUserReceiverEmailOrderByDateDesc("elena@gmail.com");
        assertTrue(notifications.isEmpty(), "La notificación debe desaparecer al cancelar la solicitud");
    }

    @Test
    @DisplayName("Should not send notification when user follows public account")
    void should_NotSendNotification_when_UserFollowsPublicAccount() {
        followService.requestFollow("paco@gmail.com", "carlos@gmail.com");

        List<Notify> notifications = notifyRepository.findByUserReceiverEmailOrderByDateDesc("carlos@gmail.com");
        assertTrue(notifications.isEmpty(), "No debe enviarse notificación de solicitud a cuentas públicas");
    }
}
