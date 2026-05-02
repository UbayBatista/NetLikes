package software.ulpgc.netlikes.repository;
import software.ulpgc.netlikes.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FilmRepository extends JpaRepository<Film, Integer>{
    List<Film> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}