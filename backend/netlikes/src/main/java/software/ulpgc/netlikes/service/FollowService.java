package software.ulpgc.netlikes.service;

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

    public List<Follow> getFollowersOf(String userId) {
        return followRepository.findByFollowerId(userId);
    }

    public List<Follow> getFollowsOf(String userId) {
        return followRepository.findByFollowedId(userId);
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
    
}
