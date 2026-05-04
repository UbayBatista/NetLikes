package software.ulpgc.netlikes.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @ManyToOne
    @MapsId("forumId")
    @JoinColumn(name = "forum_id")
    private Forum forum;
}