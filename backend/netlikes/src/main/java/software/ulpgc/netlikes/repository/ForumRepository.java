package software.ulpgc.netlikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.ulpgc.netlikes.model.Forum;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Integer> {
}