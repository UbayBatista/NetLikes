package software.ulpgc.netlikes.model;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {
    private String follower; 
    private String followed; 
}