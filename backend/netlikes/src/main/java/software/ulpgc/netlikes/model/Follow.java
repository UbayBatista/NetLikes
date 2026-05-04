package software.ulpgc.netlikes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "follow")
@IdClass(FollowId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "followerId", referencedColumnName = "email")
    private User follower;

    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "followedId", referencedColumnName = "email")
    private User followed;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;

    public String getFollowerId() {
        return follower != null ? follower.getEmail() : null;
    }

    public String getFollowedId() {
        return followed != null ? followed.getEmail() : null;
    }

    public enum State {
        PENDING,
        ACCEPTED,
        BLOCKED
    }
}