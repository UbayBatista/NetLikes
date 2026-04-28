package software.ulpgc.netlikes.service;

import org.springframework.stereotype.Service;
import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.model.SubscriptionId;
import software.ulpgc.netlikes.repository.SubscriptionRepository;
import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository){
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Subscription> getAllSubscriptions() {
        return this.subscriptionRepository.findAll();
    }

    public Subscription createSubscription(Subscription subscription) {
        return this.subscriptionRepository.save(subscription);
    }

    public List<Subscription> getByUserId(String email) {
        return this.subscriptionRepository.getByUserEmail(email);
    }

    public void deleteSubscription(SubscriptionId subscriptionId) {
        this.subscriptionRepository.deleteById(subscriptionId);
    }
}
