package software.ulpgc.netlikes.service;

import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.FollowId;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;

    public FollowService(FollowRepository followRepository, UserService userService) {
        this.followRepository = followRepository;
        this.userService = userService;
    }

    @Transactional
    public Follow requestFollow(String followerId, String followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo.");
        }

        boolean isPrivateAccount = userService.isPrivate(followedId); 
        Follow.State initialState = isPrivateAccount ? Follow.State.PENDING : Follow.State.ACCEPTED;

        Follow newFollow = new Follow(followerId, followedId, initialState);
        return followRepository.save(newFollow);
    }

    public List<Follow> getFollowByFollowerId(String followerId) {
        return followRepository.findByFollowerId(followerId);
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
