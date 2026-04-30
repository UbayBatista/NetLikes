package software.ulpgc.netlikes.service;

import org.springframework.stereotype.Service;

import software.ulpgc.netlikes.model.Forum;
import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.model.SubscriptionId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.ForumRepository;
import software.ulpgc.netlikes.repository.SubscriptionRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ForumRepository forumRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, 
            UserRepository userRepository, ForumRepository forumRepository){
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.forumRepository = forumRepository;
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

    public void subscribeUserToForum(String email, Integer filmId) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        Forum forum = forumRepository.findById(filmId)
            .orElseThrow(() -> new RuntimeException("Foro no encontrado para la película con ID: " + filmId));

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setForum(forum);
        
        this.createSubscription(subscription);
    }

}
