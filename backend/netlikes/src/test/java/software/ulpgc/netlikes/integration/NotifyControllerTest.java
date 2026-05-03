package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.Notify;
import software.ulpgc.netlikes.model.NotifyId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.NotifyRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.sql.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotifyRepository notifyRepository;

    @BeforeEach
    void setUp() {
        Date dummyDate = new Date(System.currentTimeMillis());
        
        User paco = new User();
        paco.setEmail("paco@gmail.com");
        paco.setName("Paco");
        paco.setBirthdate(dummyDate);
        paco.setPassword("123456"); 
        paco.setSecurityQuestion("¿?"); paco.setAnswer("!");
        userRepository.save(paco);

        User elena = new User();
        elena.setEmail("elena@gmail.com");
        elena.setName("Elena");
        elena.setBirthdate(dummyDate);
        elena.setPassword("123456"); 
        elena.setSecurityQuestion("¿?"); elena.setAnswer("!");
        userRepository.save(elena);

        Notify notification = new Notify();
        notification.setId(new NotifyId("paco@gmail.com", "elena@gmail.com", "Paco quiere seguirte."));
        notification.setUserSender(paco);
        notification.setUserReceiver(elena);
        notification.setDate(dummyDate);
        notification.setRead(false);
        notification.setType(Notify.Type.FOLLOWREQUEST);
        notifyRepository.save(notification);
    }

    @Test
    void testGetMyNotifications_ReturnsList() throws Exception {
        mockMvc.perform(get("/notifications/elena@gmail.com"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].senderEmail").value("paco@gmail.com"))
               .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void testGetUnreadCount_ReturnsOk() throws Exception {
        mockMvc.perform(get("/notifications/elena@gmail.com/unread-count"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.unreadCount").value(1));
    }

    @Test
    void testMarkAllAsRead_ReturnsOk() throws Exception {
        mockMvc.perform(put("/notifications/elena@gmail.com/read-all"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("Todas las notificaciones marcadas como leídas"));
    }
}