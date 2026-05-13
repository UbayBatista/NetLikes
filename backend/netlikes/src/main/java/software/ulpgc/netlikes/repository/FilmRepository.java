package software.ulpgc.netlikes.repository;
import software.ulpgc.netlikes.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FilmRepository extends JpaRepository<Film, Integer>{
    List<Film> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query(value = """
        SELECT f.* FROM film f
        WHERE f.id NOT IN (
            SELECT m.filmid FROM mark m WHERE m.email = :userEmail
            UNION
            SELECT r.film_id FROM rate r WHERE r.user_email = :userEmail
        )
        ORDER BY CAST(f.vector AS vector) <-> CAST(:userVector AS vector)
        LIMIT 50
        """, 
        nativeQuery = true)
    List<Film> findTop50Recommendations(@Param("userEmail") String userEmail, 
                                        @Param("userVector") String userVector);
}