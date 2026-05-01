package software.ulpgc.netlikes.service;

import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.FollowId;
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

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
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
}
