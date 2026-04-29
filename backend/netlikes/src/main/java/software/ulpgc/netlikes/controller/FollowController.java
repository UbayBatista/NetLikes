package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.service.FollowService;

import java.util.List;

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

    @GetMapping("/{followerId}")
    public List<Follow> getFollowByFollowerId(@PathVariable String followerId) {
        return followService.getFollowByFollowerId(followerId);
    }
    
    @PutMapping("/{id}")
    public Follow updateFollow(@PathVariable String id, @RequestBody Follow follow) {
        return followService.updateFollow(follow);
    }

    @DeleteMapping("/{followerId}/{followedId}")
    public void deleteParticipate(@PathVariable String followerId, @PathVariable String followedId) {
        followService.deleteFollow(followerId, followedId);
    }
}
