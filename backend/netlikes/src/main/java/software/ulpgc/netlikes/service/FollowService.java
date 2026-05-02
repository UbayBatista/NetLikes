package software.ulpgc.netlikes.service;

import software.ulpgc.netlikes.service.DiscourseService;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.FollowId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final DiscourseService discourseService;

    public FollowService(FollowRepository followRepository, 
                         UserRepository userRepository, 
                         DiscourseService discourseService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.discourseService = discourseService;
    }

    @Transactional
    public Follow requestFollow(@NonNull String followerId, @NonNull String followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo.");
        }

        boolean isPrivateAccount = userRepository.findById(followedId)
                                    .orElseThrow(() -> new RuntimeException("User not found"))
                                    .isAccountPrivacity();
        Follow.State initialState = isPrivateAccount ? Follow.State.PENDING : Follow.State.ACCEPTED;

        Follow newFollow = new Follow(followerId, followedId, initialState);
        return followRepository.save(newFollow);
    }

    public String checkStatus(String followerId, String followedId) {
        return followRepository.findById(new FollowId(followerId, followedId))
                .map(follow -> follow.getState().name())
                .orElse("NONE");
    }

    public List<UserResponseDTO> getFollowersOf(String targetEmail) {
        return followRepository.findByFollowedId(targetEmail).stream()
                .filter(follow -> follow.getState() == Follow.State.ACCEPTED)
                .map(follow -> {
                    return userRepository.findById(follow.getFollowerId())
                            .map(user -> new UserResponseDTO(
                                    user.getEmail(), 
                                    user.getName(), 
                                    user.getProfilePicture()
                            ))
                            .orElse(null);
                })
                .filter(dto -> dto != null) 
                .toList();
    }

    public List<UserResponseDTO> getFollowsOf(String targetEmail) {
        return followRepository.findByFollowerId(targetEmail).stream()
                .filter(follow -> follow.getState() == Follow.State.ACCEPTED)
                .map(follow -> {
                    return userRepository.findById(follow.getFollowedId())
                            .map(user -> new UserResponseDTO(
                                    user.getEmail(), 
                                    user.getName(), 
                                    user.getProfilePicture()
                            ))
                            .orElse(null);
                })
                .filter(dto -> dto != null)
                .toList();
    }

    public Integer countFollowersOf(String userId) {
        return (int)this.getFollowersOf(userId).stream().count();
    }

    public Integer countFollowsOf(String userId) {
        return (int)this.getFollowsOf(userId).stream().count();
    }

    public Follow updateFollow(Follow follow) {
        return followRepository.findById(new FollowId(follow.getFollowerId(), follow.getFollowedId()))
                .map(existingFollow -> {
                    existingFollow.setState(follow.getState());
                    return followRepository.save(existingFollow);
                })
                .orElse(null);
    }

    public void deleteFollow(String followerId, String followedId) {
        followRepository.deleteById(new FollowId(followerId, followedId));
    }

    @Transactional
    public void blockUser(@NonNull String blockerEmail, @NonNull String blockedEmail) {
        if (blockerEmail.equals(blockedEmail)) {
            throw new IllegalArgumentException("Un usuario no puede bloquearse a sí mismo.");
        }

        User blocker = userRepository.findById(blockerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario que bloquea no encontrado"));
        User blocked = userRepository.findById(blockedEmail)
                .orElseThrow(() -> new RuntimeException("Usuario a bloquear no encontrado"));

        String blockerUsername = blocker.getName().replaceAll("\\s+", "").toLowerCase();
        String blockedUsername = blocked.getName().replaceAll("\\s+", "").toLowerCase();

        discourseService.ignoreDiscourseUser(blockerUsername, blockedUsername);

        followRepository.findById(new FollowId(blockerEmail, blockedEmail))
                .ifPresent(followRepository::delete);
                
        followRepository.findById(new FollowId(blockedEmail, blockerEmail))
                .ifPresent(followRepository::delete);

    }

    @Transactional
    public void unblockUser(@NonNull String blockerEmail, @NonNull String unblockedEmail) {
        User blocker = userRepository.findById(blockerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User unblocked = userRepository.findById(unblockedEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String blockerUsername = blocker.getName().replaceAll("\\s+", "").toLowerCase();
        String unblockedUsername = unblocked.getName().replaceAll("\\s+", "").toLowerCase();

        discourseService.unignoreDiscourseUser(blockerUsername, unblockedUsername);

        followRepository.deleteById(new FollowId(blockerEmail, unblockedEmail));
    }
    
}
