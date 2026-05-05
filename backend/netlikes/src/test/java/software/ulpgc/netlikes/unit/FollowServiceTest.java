package software.ulpgc.netlikes.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.FollowId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.FollowService;
import software.ulpgc.netlikes.service.NotifyService;
import software.ulpgc.netlikes.service.DiscourseService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotifyService notifyService;

    @Mock
    private DiscourseService discourseService;

    @InjectMocks
    private FollowService followService;

    private User paco;
    private User privateUser;
    private Follow pendingFollow;

    @BeforeEach
    void setUp() {
        paco = new User();
        paco.setEmail("paco@gmail.com");

        privateUser = new User();
        privateUser.setEmail("privado@gmail.com");
        privateUser.setAccountPrivacity(true);

        pendingFollow = new Follow(paco, privateUser, Follow.State.PENDING);
    }

    @Test
    void testRequestFollow_PrivateAccount_CreatesPendingAndNotifies() {
        when(userRepository.findById("privado@gmail.com")).thenReturn(Optional.of(privateUser));
        when(userRepository.findById("paco@gmail.com")).thenReturn(Optional.of(paco));
        when(followRepository.save(any(Follow.class))).thenAnswer(i -> i.getArguments()[0]);

        Follow result = followService.requestFollow("paco@gmail.com", "privado@gmail.com");

        assertEquals(Follow.State.PENDING, result.getState());
        assertEquals("paco@gmail.com", result.getFollower().getEmail());
        verify(followRepository, times(1)).save(any(Follow.class));
        verify(notifyService, times(1)).createFollowNotification("paco@gmail.com", "privado@gmail.com");
    }

    @Test
    void testAcceptFollow_UpdatesStateToAccepted() {
        when(followRepository.findById(new FollowId("paco@gmail.com", "privado@gmail.com")))
                .thenReturn(Optional.of(pendingFollow));
        when(followRepository.save(any(Follow.class))).thenAnswer(i -> i.getArguments()[0]);

        Follow result = followService.acceptFollow("paco@gmail.com", "privado@gmail.com");

        assertEquals(Follow.State.ACCEPTED, result.getState());
        verify(followRepository, times(1)).save(pendingFollow);
    }

    @Test
    void testRejectFollow_DeletesFollowRecord() {
        followService.rejectFollow("paco@gmail.com", "privado@gmail.com");

        verify(followRepository, times(1)).deleteById(new FollowId("paco@gmail.com", "privado@gmail.com"));
    }

    @Test
    void testRequestFollow_PublicAccount_CreatesAcceptedAndNoNotification() {
        User publicUser = new User();
        publicUser.setEmail("publico@gmail.com");
        publicUser.setAccountPrivacity(false); 

        when(userRepository.findById("publico@gmail.com")).thenReturn(Optional.of(publicUser));
        when(userRepository.findById("paco@gmail.com")).thenReturn(Optional.of(paco));
        when(followRepository.save(any(Follow.class))).thenAnswer(i -> i.getArguments()[0]);

        Follow result = followService.requestFollow("paco@gmail.com", "publico@gmail.com");

        assertEquals(Follow.State.ACCEPTED, result.getState());
        verify(followRepository, times(1)).save(any(Follow.class));
        verify(notifyService, never()).createFollowNotification(anyString(), anyString()); 
    }

    @Test
    void testDeleteFollow_DeletesFollowAndNotification() {
        followService.deleteFollow("paco@gmail.com", "privado@gmail.com");

        verify(followRepository, times(1)).deleteById(new FollowId("paco@gmail.com", "privado@gmail.com"));
        verify(notifyService, times(1)).deleteFollowNotification("paco@gmail.com", "privado@gmail.com");
    }
}