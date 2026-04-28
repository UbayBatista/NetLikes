package software.ulpgc.netlikes.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionId implements Serializable  {
    private String email;
    private Integer forumId;
}
