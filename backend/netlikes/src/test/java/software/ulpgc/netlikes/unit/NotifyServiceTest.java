package software.ulpgc.netlikes.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.ulpgc.netlikes.model.Notify;
import software.ulpgc.netlikes.repository.NotifyRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.NotifyService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import software.ulpgc.netlikes.model.User;

@ExtendWith(MockitoExtension.class)
public class NotifyServiceTest {

    @Mock private NotifyRepository notifyRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotifyService notifyService;

    @Test
    void testDeleteFollowNotification_CallsRepository() {
        notifyService.deleteFollowNotification("paco@gmail.com", "elena@gmail.com");

        verify(notifyRepository, times(1)).deleteByUserSenderEmailAndUserReceiverEmailAndType(
                "paco@gmail.com",
                "elena@gmail.com",
                Notify.Type.FOLLOWREQUEST
        );
    }
    
    @Test
    void testMarkAllAsRead_CallsRepository() {
        notifyService.markAllAsRead("elena@gmail.com");
        
        verify(notifyRepository, times(1)).markAllAsReadForUser("elena@gmail.com");
    }

    @Test
    void testCreateFollowNotification_SavesNotificationCorrectly() {
        User paco = new User(); paco.setEmail("paco@gmail.com"); paco.setName("Paco");
        User elena = new User(); elena.setEmail("elena@gmail.com"); elena.setName("Elena");

        when(userRepository.findById("paco@gmail.com")).thenReturn(java.util.Optional.of(paco));
        when(userRepository.findById("elena@gmail.com")).thenReturn(java.util.Optional.of(elena));

        notifyService.createFollowNotification("paco@gmail.com", "elena@gmail.com");

        verify(notifyRepository, times(1)).save(org.mockito.ArgumentMatchers.argThat(notification -> 
            notification.getType() == Notify.Type.FOLLOWREQUEST &&
            notification.getUserSender().getEmail().equals("paco@gmail.com") &&
            notification.getUserReceiver().getEmail().equals("elena@gmail.com") &&
            !notification.isRead()
        ));
    }
}