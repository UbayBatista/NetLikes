package software.ulpgc.netlikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import software.ulpgc.netlikes.model.Recommend;
import software.ulpgc.netlikes.model.RecommendId;
import software.ulpgc.netlikes.dto.RecommendCountDTO;
import java.util.List;

public interface RecommendRepository extends JpaRepository<Recommend, RecommendId> {
    List<Recommend> findByRecommendedEmail(String email);
    
    @Query("SELECT new software.ulpgc.netlikes.model.RecommendCountDTO(r.film, COUNT(r)) " +
           "FROM Recommend r " +
           "WHERE r.recommended.email = :email " +
           "GROUP BY r.film")
    List<RecommendCountDTO> countRecommendationsByUser(@Param("email") String email);
}
