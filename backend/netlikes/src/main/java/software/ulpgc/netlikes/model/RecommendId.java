package software.ulpgc.netlikes.model;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendId implements Serializable {
    private String recommender; 
    private String recommended; 
    private Integer film;
}