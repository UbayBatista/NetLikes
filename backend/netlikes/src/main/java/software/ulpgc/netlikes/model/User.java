package software.ulpgc.netlikes.model;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class User {
    @Id private String email;
    @Column(nullable=false) private String password;
    @Column(nullable=false) private String securityQuestion;
    @Column(nullable=false) private String answer;
    @Column(nullable=false, unique=true) private String name;
    @Column(nullable=false) private Date birthdate;
    @Column(nullable=false) private boolean accountPrivacity;
    @Column(nullable=false) private boolean showWatchedFilms;
    @Column(nullable=false) private boolean showFilmsToWatchLater;
    @Column(nullable=false) private boolean showRecommendedFilms;
    @Column(nullable=true) private String profilePicture;
    @Column(nullable=true) private String bio;
    @Column(nullable=true) private String discourseId;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Mark> markFilms;

    @ManyToMany
    @Size(min=3 , message="Tiene que seleccionar al menos un género favorito")
    @JoinTable(
        name = "mark_favorite",
        joinColumns = @JoinColumn(name = "userId"),
        inverseJoinColumns = @JoinColumn(name = "genreId")
    )
    private List<Genre> favoriteGenres;

    private String vector;
}
