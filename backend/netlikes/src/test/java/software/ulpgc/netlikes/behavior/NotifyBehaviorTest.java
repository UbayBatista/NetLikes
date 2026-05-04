package software.ulpgc.netlikes.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import software.ulpgc.netlikes.model.Notify;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.NotifyRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.FollowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@Transactional
public class NotifyBehaviorTest {

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
        userRepository.save(paco);

        User elena = new User();
        elena.setEmail("elena@gmail.com");
        elena.setName("Elena");
        elena.setAccountPrivacity(true);
        elena.setBirthdate(dummyDate);
        elena.setPassword("123456");
        elena.setSecurityQuestion("?");
        elena.setAnswer("!");
        userRepository.save(elena);

        User carlos = new User();
        carlos.setEmail("carlos@gmail.com");
        carlos.setName("Carlos");
        carlos.setAccountPrivacity(false);
        carlos.setBirthdate(dummyDate);
        carlos.setPassword("123456");
        carlos.setSecurityQuestion("?");
        carlos.setAnswer("!");
        userRepository.save(carlos);
    }

    @Test
    void testHU8_2_ReceiveNotificationOnFollowRequest() {
        followService.requestFollow("paco@gmail.com", "elena@gmail.com");

        List<Notify> notifs = notifyRepository.findByUserReceiverEmailOrderByDateDesc("elena@gmail.com");
        assertEquals(1, notifs.size(), "Debe existir una notificación en el buzón");
        assertEquals(Notify.Type.FOLLOWREQUEST, notifs.get(0).getType());
        assertEquals("Paco quiere seguirte.", notifs.get(0).getId().getMessage());
        assertFalse(notifs.get(0).isRead(), "La notificación debe estar pendiente (no leída)");
    }

    @Test
    void testCancelFollowRequest_RemovesNotification() {
        followService.requestFollow("paco@gmail.com", "elena@gmail.com");

        followService.deleteFollow("paco@gmail.com", "elena@gmail.com");

        List<Notify> notifs = notifyRepository.findByUserReceiverEmailOrderByDateDesc("elena@gmail.com");
        assertTrue(notifs.isEmpty(), "La notificación debe desaparecer al cancelar la solicitud");
    }

    @Test //Este test habrá que quitarlo cuando seguir genere notifiaciones.
    void testFollowPublicAccount_DoesNotSendNotification() {
        followService.requestFollow("paco@gmail.com", "carlos@gmail.com");

        List<Notify> notifs = notifyRepository.findByUserReceiverEmailOrderByDateDesc("carlos@gmail.com");
        assertTrue(notifs.isEmpty(), "No debe enviarse notificación de solicitud a cuentas públicas");
    }
}