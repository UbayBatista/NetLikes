package software.ulpgc.netlikes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import software.ulpgc.netlikes.model.Notify;
import software.ulpgc.netlikes.model.NotifyId;

import java.util.List;

public interface NotifyRepository extends JpaRepository<Notify, NotifyId> {
    
    List<Notify> findByUserReceiverEmailOrderByDateDesc(String emailReceiver);

    long countByUserReceiverEmailAndReadFalse(String emailReceiver);

    @Modifying
    @Query("UPDATE Notify n SET n.read = true WHERE n.userReceiver.email = :email AND n.read = false")
    void markAllAsReadForUser(@Param("email") String email);
}