package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.model.Subscription;
import software.ulpgc.netlikes.model.SubscriptionId;
import software.ulpgc.netlikes.service.SubscriptionService;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/subscribe_to")
@CrossOrigin(origins = "http://localhost:4200")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService){
        this.subscriptionService = subscriptionService;
    }

    @GetMapping  
    public List<Subscription> getAll(){
        return this.subscriptionService.getAllSubscriptions();
    }

    @GetMapping("/{email}")
    public List<Subscription> getByUserId(@PathVariable String email) {
        return this.subscriptionService.getByUserId(email);
    }
    
    @PostMapping
    public Subscription create(@Valid @RequestBody Subscription subscription){
        return this.subscriptionService.createSubscription(subscription);
    }

    @DeleteMapping("/{email}/unsuscribe/{forumId}")
    public void delete(@PathVariable String email, @PathVariable Integer forumId) {
        this.subscriptionService.deleteSubscription(new SubscriptionId(email, forumId));
    }
}