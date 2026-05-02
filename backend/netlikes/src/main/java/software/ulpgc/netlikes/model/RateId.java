package software.ulpgc.netlikes.model;

import lombok.*;
import java.io.Serializable;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateId implements Serializable {
    private String email;
    private Integer film;
}