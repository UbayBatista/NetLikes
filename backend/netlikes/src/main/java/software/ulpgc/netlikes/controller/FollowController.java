package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.service.FollowService;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService){
        this.followService = followService;
    }
    
    @PostMapping("/{targetId}")
    public ResponseEntity<Follow> followUser(
            @PathVariable String targetId, 
            @RequestHeader("X-User-Id") String myId) {
        
        Follow result = followService.requestFollow(myId, targetId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{targetId}/status")
    public ResponseEntity<Map<String, String>> checkFollowStatus(
            @PathVariable String targetId,
            @RequestHeader("X-User-Id") String myId) {
        
        String status = followService.checkStatus(myId, targetId);

        return ResponseEntity.ok(Map.of("state", status)); 
    }

    @GetMapping("/followersOf/{followerId}")
    public List<UserResponseDTO> getFollowersOf(@PathVariable String followerId) {
        return followService.getFollowersOf(followerId);
    }

    @GetMapping("/followsOf/{followerId}")
    public List<UserResponseDTO> getFollowsOf(@PathVariable String followerId) {
        return followService.getFollowsOf(followerId);
    }
    
    @PutMapping("/{id}")
    public Follow updateFollow(@PathVariable String id, @RequestBody Follow follow) {
        return followService.updateFollow(follow);
    }

    @DeleteMapping("/{targetId}/unfollow")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable String targetId, 
            @RequestHeader("X-User-Id") String myId) {
        
        followService.deleteFollow(myId, targetId);
        
        return ResponseEntity.noContent().build();
    }
}
