package software.ulpgc.netlikes.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {

    @Mock private FollowRepository followRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotifyService notifyService;
    @Mock private DiscourseService discourseService;

    @InjectMocks
    private FollowService followService;

    private User paco;
    private User publicUser;
    private User privateUser;
    private Follow pendingFollow;

    @BeforeEach
    void setUp() {
        paco = new User();
        paco.setEmail("paco@gmail.com");
        paco.setVector("");

        privateUser = new User();
        privateUser.setEmail("privado@gmail.com");
        privateUser.setAccountPrivacity(true);
        privateUser.setVector("");
        pendingFollow = new Follow(paco, privateUser, Follow.State.PENDING);

        publicUser = new User();
        publicUser.setEmail("publico@gmail.com");
        publicUser.setAccountPrivacity(false); 
        publicUser.setVector("");

        lenient().when(userRepository.findById("privado@gmail.com")).thenReturn(Optional.of(privateUser));
        lenient().when(userRepository.findById("paco@gmail.com")).thenReturn(Optional.of(paco));
        lenient().when(userRepository.findById("publico@gmail.com")).thenReturn(Optional.of(publicUser));

        lenient().when(followRepository.save(any(Follow.class))).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(followRepository.findById(new FollowId("paco@gmail.com", "privado@gmail.com")))
                .thenReturn(Optional.of(pendingFollow));
        
        lenient().when(discourseService.getRealUsernameByEmail("paco@gmail.com")).thenReturn("PacoForo");
        lenient().when(discourseService.getRealUsernameByEmail("privado@gmail.com")).thenReturn("PrivadoForo");
    }

    @Test
    @DisplayName("Should create pending follow and notify user when requesting follow to private account")
    void should_CreatePendingFollowAndNotifyUser_when_RequestingFollowToPrivateAccount() {
        Follow result = followService.requestFollow("paco@gmail.com", "privado@gmail.com");

        assertEquals(Follow.State.PENDING, result.getState());
        assertEquals("paco@gmail.com", result.getFollower().getEmail());
        verify(followRepository, times(1)).save(any(Follow.class));
        verify(notifyService, times(1))
                    .createFollowNotification("paco@gmail.com", "privado@gmail.com");
    }

    @Test
    @DisplayName("Should accept follow request and update status to ACCEPTED")
    void should_UpdatesStateToAccepted_when_AcceptingFollowRequest() {
        Follow result = followService.acceptFollow("paco@gmail.com", "privado@gmail.com");

        assertEquals(Follow.State.ACCEPTED, result.getState());
        verify(followRepository, times(1)).save(pendingFollow);
    }

    @Test
    @DisplayName("Should reject follow request and remove it from database")
    void should_DeleteFollowRecord_when_RejectingFollowRequest() {
        followService.rejectFollow("paco@gmail.com", "privado@gmail.com");

        verify(followRepository, times(1))
                    .deleteById(new FollowId("paco@gmail.com", "privado@gmail.com"));
    }

    @Test
    @DisplayName("Should follow public account without needing acceptance")
    void should_CreateAcceptedFollowAndNotNotifyUser_when_RequestingFollowToPublicAccount() {
        Follow result = followService.requestFollow("paco@gmail.com", "publico@gmail.com");

        assertEquals(Follow.State.ACCEPTED, result.getState());
        verify(followRepository, times(1)).save(any(Follow.class));
        verify(notifyService, never()).createFollowNotification(anyString(), anyString()); 
    }

    @Test
    @DisplayName("Should delete follow record when unfollowing user")
    void should_DeleteFollowRecord_when_DeletingFollow() {
        followService.deleteFollow("paco@gmail.com", "privado@gmail.com");

        verify(followRepository, times(1))
                    .deleteById(new FollowId("paco@gmail.com", "privado@gmail.com"));
        verify(notifyService, times(1))
                    .deleteFollowNotification("paco@gmail.com", "privado@gmail.com");
    }

    @Test
    @DisplayName("Should block user and update status to BLOCKED")
    void should_SavesBlockedStateAndIgnoresInDiscourse_when_BlockingUser() {
        followService.blockUser("paco@gmail.com", "privado@gmail.com");

        verify(discourseService, times(1)).ignoreDiscourseUser("PacoForo", "PrivadoForo");
        verify(followRepository, times(1)).save(argThat(follow -> 
            follow.getFollower().getEmail().equals("paco@gmail.com") &&
            follow.getFollowed().getEmail().equals("privado@gmail.com") &&
            follow.getState() == Follow.State.BLOCKED
        ));
    }

    @Test
    @DisplayName("Should throw exception when user blocks himself")
    void should_ThrowsException_when_UserBlocksHimself() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            followService.blockUser("paco@gmail.com", "paco@gmail.com");
        });
        
        assertEquals("Un usuario no puede bloquearse a sí mismo.", exception.getMessage());
        verify(followRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should unblock user and remove follow record from database")
    void should_DeletesRecordAndUnignoresInDiscourse_when_UnblockingUser() {
        followService.unblockUser("paco@gmail.com", "privado@gmail.com");

        verify(discourseService, times(1)).unignoreDiscourseUser("PacoForo", "PrivadoForo");
        verify(followRepository, times(1))
                    .deleteById(new FollowId("paco@gmail.com", "privado@gmail.com"));
    }
}