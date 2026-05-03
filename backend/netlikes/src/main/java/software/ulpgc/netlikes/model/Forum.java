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

    @Column(unique = true, nullable = false)
    private Integer discourseTopicId; 
}
