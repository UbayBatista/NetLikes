package software.ulpgc.netlikes.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity 
@Table(name = "mark")
@Data
@NoArgsConstructor

public class Mark {

    @EmbeddedId
    private MarkId id;

    @ManyToOne
    @MapsId("email")
    @JoinColumn(name = "email")
    @JsonIgnore
    private User user;

    @ManyToOne
    @MapsId("filmId")
    @JoinColumn(name = "film_id")
    @JsonIgnore
    private Film film;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    public enum Type {
        SEEN,
        WATCHLATER
    } 
}