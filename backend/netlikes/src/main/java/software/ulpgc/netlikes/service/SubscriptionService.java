package software.ulpgc.netlikes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ForumService forumService;
    private final DiscourseService discourseService;
    private final ForumRepository forumRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, ForumService forumService, DiscourseService discourseService, ForumRepository forumRepository){
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.forumService = forumService;
        this.discourseService = discourseService; 
        this.forumRepository = forumRepository;
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

        // Forum forum = forumService.getOrCreateForum(filmId, filmTitle);
        Forum forum = forumRepository.findById(filmId)
            .orElseThrow(() -> new RuntimeException("Foro no encontrado para la película con ID: " + filmId));

        Subscription subscription = new Subscription();
        subscription.setId(new SubscriptionId(email, forum.getId()));
        subscription.setUser(user);
        subscription.setForum(forum);

        return this.subscriptionRepository.save(subscription);
    }

    @Transactional
    public void deleteSubscription(String email, Integer forumId) {
        
        long currentSubscriptions = this.subscriptionRepository.countByIdForumId(forumId);

        SubscriptionId id = new SubscriptionId(email, forumId);
        this.subscriptionRepository.deleteById(id);
        
        this.subscriptionRepository.flush(); 

        if (currentSubscriptions <= 1) {
            Forum forum = forumService.getAllForums().stream()
                .filter(f -> f.getId().equals(forumId))
                .findFirst()
                .orElse(null);

            if (forum != null) {
                Integer postCount = discourseService.getTopicPostCount(forum.getDiscourseTopicId());

                if (postCount != null && postCount <= 1) {
                    forumService.deleteForum(forumId);
                }
            }
        }
    }
}
