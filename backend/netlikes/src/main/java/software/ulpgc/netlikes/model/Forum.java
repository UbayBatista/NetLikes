package software.ulpgc.netlikes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity 
@Table(name = "forum")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Forum {
    
    @Id
    private Integer id; 

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Film film;

    // Aquí guardaremos el ID que nos devuelve Discourse (topic_id)
    @Column(name = "discourse_topic_id", unique = true, nullable = false)
    private Integer forumId; 
}
