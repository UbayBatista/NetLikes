package software.ulpgc.netlikes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.model.SubscriptionId;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    List<Subscription> getByUserEmail(String email);
}
