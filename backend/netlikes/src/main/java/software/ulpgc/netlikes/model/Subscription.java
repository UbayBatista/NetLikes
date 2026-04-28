package software.ulpgc.netlikes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="subscribe_to")
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class Subscription {
    @EmbeddedId
    private SubscriptionId id = new SubscriptionId();

    @ManyToOne
    @MapsId("email")
    @JoinColumn(name = "email")
    private User user;

    @ManyToOne
    @MapsId("forumId")
    @JoinColumn(name = "forum_id")
    private Forum forum;
}