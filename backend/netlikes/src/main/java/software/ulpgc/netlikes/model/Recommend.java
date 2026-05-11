package software.ulpgc.netlikes.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Date;
 

@Entity
@Table(name = "recommend")
@IdClass(RecommendId.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recommend {

    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recommender_email", referencedColumnName = "email")
    private User recommender;

    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "recommended_email", referencedColumnName = "email")
    private User recommended;

    @Id
    @ManyToOne
    @MapsId("film")
    @JoinColumn(name = "film_id")
    @JsonIgnore
    private Film film;

    @Column(nullable = false)
    private Date date;
}
