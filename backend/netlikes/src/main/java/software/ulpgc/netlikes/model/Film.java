package software.ulpgc.netlikes.model;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity 
@Table(name = "film")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Film{
    @Id
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String overView;

    @Column(nullable = false)
    private boolean adult;

    @Column(columnDefinition = "TEXT")
    private String ageRating;

    @Column(columnDefinition = "TEXT")
    private String tagLine;

    @Column(columnDefinition = "INTEGER")
    private Integer runtime;

    @Column()
    private Date releaseDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String posterPath;

    @ManyToMany
    @JoinTable(name = "available", 
        joinColumns = @JoinColumn(name = "filmId"),
        inverseJoinColumns = @JoinColumn(name = "platformId")    
    )

    private List<Platform> watchProviders;

    @OneToMany(mappedBy = "film")
    @JsonIgnore
    private List<Mark> markUsers;
    
    @ManyToMany
    @JoinTable(
        name = "belong_to",
        joinColumns = @JoinColumn(name = "filmId"),
        inverseJoinColumns = @JoinColumn(name = "genreId")
    )
    private List<Genre> genres;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL)
    private Set<Participate> cast;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Video> videos;

    @OneToOne(mappedBy = "film", cascade = CascadeType.ALL)
    private Forum forum;
}