package software.ulpgc.netlikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import software.ulpgc.netlikes.model.Recommend;
import software.ulpgc.netlikes.model.RecommendId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.dto.RecommendCountDTO;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RecommendRepository extends JpaRepository<Recommend, RecommendId> {
    
    List<Recommend> findByRecommendedEmail(String email);
    
    @Query("SELECT new software.ulpgc.netlikes.dto.RecommendCountDTO(r.film, COUNT(r)) " +
           "FROM Recommend r " +
           "WHERE r.recommended.email = :email " +
           "GROUP BY r.film")
    List<RecommendCountDTO> countRecommendationsByUser(@Param("email") String email);

    @Query("SELECT r.recommended FROM Recommend r " +
       "WHERE r.recommender.email = :email " +
       "GROUP BY r.recommended " +
       "ORDER BY MAX(r.date) DESC")
    List<User> findLastRecipients(@Param("email") String email, Pageable pageable);

    @Query("SELECT r.recommended.email FROM Recommend r " +
           "WHERE r.recommender.email = :recommenderEmail AND r.film.id = :filmId")
    List<String> findRecipientsByFilm(@Param("recommenderEmail") String recommenderEmail, @Param("filmId") Integer filmId);
}