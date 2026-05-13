package software.ulpgc.netlikes.model;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity 
@Table(name = "mark")
@IdClass(MarkId.class)
@Data
@NoArgsConstructor

public class Mark {

    @Id
    @ManyToOne
    @JoinColumn(name = "email")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "filmid")
    @JsonIgnore
    private Film film;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    public enum Type {
        SEEN,
        WATCHLATER,
        RECOMMENDED
    } 
}