package software.ulpgc.netlikes.repository;
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.model.MarkId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, MarkId>{
    List<Mark> findByUserEmailAndType(String email, Mark.Type type);
}