package software.ulpgc.netlikes.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
    @JoinColumn(name = "email", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "film_id", insertable = false, updatable = false)
    @JsonIgnore
    private Film film;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", insertable = false, updatable = false)
    private Type type;

    public enum Type {
        SEEN,
        WATCHLATER,
        RECOMMENDED
    } 
}