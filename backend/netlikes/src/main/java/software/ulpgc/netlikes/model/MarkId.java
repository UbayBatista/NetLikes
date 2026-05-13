package software.ulpgc.netlikes.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkId implements Serializable {
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "film_id")
    private Integer filmId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Mark.Type type;
}