package software.ulpgc.netlikes.repository;
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.model.MarkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import software.ulpgc.netlikes.model.Film;
import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, MarkId>{
    @Query("SELECT m.film FROM Mark m WHERE m.user.email = :email AND m.type = :type")
    List<Film> findFilmsByUserEmailAndType(@Param("email") String email, @Param("type") Mark.Type type);
    
    @Query("SELECT m.type FROM Mark m WHERE m.user.email = :email AND m.film.id = :filmId")
    List<Mark.Type> findTypesByUserEmailAndFilmId(@Param("email") String email, @Param("filmId") Integer filmId);
    
    boolean existsByUserEmailAndFilmIdAndType(String email, Integer filmId, Mark.Type type);
    
    void deleteByUserEmailAndFilmIdAndType(String email, Integer filmId, Mark.Type type);
}