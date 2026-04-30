package software.ulpgc.netlikes.repository;

import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.FollowId;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    List<Follow> findByFollowerId(String followerId);
    List<Follow> findByFollowedId(String followedId);
}
