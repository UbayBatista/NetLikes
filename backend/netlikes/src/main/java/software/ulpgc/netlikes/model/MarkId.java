package software.ulpgc.netlikes.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkId implements Serializable {
    private String user;
    private Integer film;
    private Mark.Type type;
}
