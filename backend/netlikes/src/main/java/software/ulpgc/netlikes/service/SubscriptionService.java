package software.ulpgc.netlikes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.Forum;
import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.model.SubscriptionId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.SubscriptionRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.List;

@Service
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ForumService forumService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, ForumService forumService){
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.forumService = forumService;
    }

    public List<Subscription> getAllSubscriptions() {
        return this.subscriptionRepository.findAll();
    }

    public List<Subscription> getByUserId(String email) {
        return this.subscriptionRepository.getByUserEmail(email);
    }

    @Transactional
    public Subscription subscribeUserToFilm(String email, Integer filmId) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("UserNotFound"));

        Forum forum = forumService.createForumForFilmId(filmId);

        Subscription subscription = new Subscription();
        subscription.setId(new SubscriptionId(email, forum.getId()));
        subscription.setUser(user);
        subscription.setForum(forum);

        return this.subscriptionRepository.save(subscription);
    }

    @Transactional
    public void deleteSubscription(String email, Integer forumId) {
        SubscriptionId id = new SubscriptionId(email, forumId);
        this.subscriptionRepository.deleteById(id);
    }
}
