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
    private final NotifyService notifyService;

    public FollowService(FollowRepository followRepository, 
                         UserRepository userRepository, 
                         DiscourseService discourseService,
                         NotifyService notifyService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.discourseService = discourseService;
        this.notifyService = notifyService;
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
        Follow saved = followRepository.save(newFollow);

        if (initialState == Follow.State.PENDING) {
            notifyService.createFollowNotification(followerId, followedId);
        }

        return saved;
    }

    public String checkStatus(String followerId, String followedId) {
        var directRelation = followRepository.findById(new FollowId(followerId, followedId));
        if (directRelation.isPresent() && directRelation.get().getState() == Follow.State.BLOCKED) {
            return "BLOCKED";
        }

        var reverseRelation = followRepository.findById(new FollowId(followedId, followerId));
        if (reverseRelation.isPresent() && reverseRelation.get().getState() == Follow.State.BLOCKED) {
            return "BLOCKED";
        }

        return directRelation
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
        notifyService.deleteFollowNotification(followerId, followedId);
    }

    public List<UserResponseDTO> getPendingRequests(String followedId) {
        return followRepository.findByFollowedId(followedId).stream()
            .filter(follow -> follow.getState() == Follow.State.PENDING)
            .map(follow -> userRepository.findById(follow.getFollowerId())
                .map(user -> new UserResponseDTO(
                    user.getEmail(),
                    user.getName(),
                    user.getProfilePicture()
                ))
                .orElse(null))
            .filter(dto -> dto != null)
            .toList();
    }

    public Follow acceptFollow(String followerId, String followedId) {
        Follow follow = followRepository.findById(new FollowId(followerId, followedId))
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        follow.setState(Follow.State.ACCEPTED);
        return followRepository.save(follow);
    }

    public void rejectFollow(String followerId, String followedId) {
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

        String blockerUsername = discourseService.getRealUsernameByEmail(blockerEmail);
        String blockedUsername = discourseService.getRealUsernameByEmail(blockedEmail);

        if (blockerUsername != null && blockedUsername != null) {
            try {
                discourseService.ignoreDiscourseUser(blockerUsername, blockedUsername);
            } catch (Exception e) {
                System.out.println("Aviso: No se pudo bloquear en Discourse. " + e.getMessage());
            }
        } else {
            System.out.println("⏭️ Omitiendo bloqueo en Discourse: Al menos uno de los usuarios no tiene cuenta en el foro.");
        }

        followRepository.findById(new FollowId(blockerEmail, blockedEmail))
                .ifPresent(followRepository::delete);
                
        followRepository.findById(new FollowId(blockedEmail, blockerEmail))
                .ifPresent(followRepository::delete);

        Follow follow = new Follow();
        follow.setFollowedId(blockedEmail);
        follow.setFollowerId(blockerEmail);
        follow.setState(Follow.State.BLOCKED);
        followRepository.save(follow);

    }

    @Transactional
    public void unblockUser(@NonNull String blockerEmail, @NonNull String unblockedEmail) {
        User blocker = userRepository.findById(blockerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User unblocked = userRepository.findById(unblockedEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String blockerUsername = discourseService.getRealUsernameByEmail(blockerEmail);
        String unblockedUsername = discourseService.getRealUsernameByEmail(unblockedEmail);

        try {
            discourseService.unignoreDiscourseUser(blockerUsername, unblockedUsername);
        } catch (Exception e) {
            System.out.println("Aviso: No se pudo desbloquear en Discourse (posiblemente el usuario no ha entrado nunca). " + e.getMessage());
        }

        followRepository.deleteById(new FollowId(blockerEmail, unblockedEmail));
    }

    public List<UserResponseDTO> getBlockedUsers(String userEmail) {
        return followRepository.findByFollowerId(userEmail).stream()
                .filter(follow -> follow.getState() == Follow.State.BLOCKED)
                .map(follow -> userRepository.findById(follow.getFollowedId())
                        .map(user -> new UserResponseDTO(
                                user.getEmail(), 
                                user.getName(), 
                                user.getProfilePicture()
                        ))
                        .orElse(null))
                .filter(dto -> dto != null)
                .toList();
    }
    
}
