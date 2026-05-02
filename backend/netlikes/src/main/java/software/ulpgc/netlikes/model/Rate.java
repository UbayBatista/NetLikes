package software.ulpgc.netlikes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rate")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rate {
    @EmbeddedId
    private RateId id;

    @ManyToOne
    @MapsId("email")
    @JoinColumn(name = "user_email")
    @JsonIgnore
    private User user;

    @ManyToOne
    @MapsId("film")
    @JoinColumn(name = "film_id")
    @JsonIgnore
    private Film film;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Score score;

    public enum Score {
        DISLIKE, LIKE, LOVE
    }
}
