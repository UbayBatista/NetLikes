package software.ulpgc.netlikes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Date;
import jakarta.persistence.*;
import lombok.*;

@Entity 
@Table(name = "notify")
@Data
@NoArgsConstructor
public class Notify {

     @EmbeddedId
    private NotifyId id;

    @ManyToOne
    @MapsId("emailSender")
    @JoinColumn(name = "email_sender")
    @JsonIgnore
    private User userSender;

    @ManyToOne
    @MapsId("emailReceiver")
    @JoinColumn(name = "email_receiver")
    @JsonIgnore
    private User userReceiver;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    public enum Type {
        FOLLOWREQUEST //TO DO: Añadir más tipos para futuras notificaciones
    } 
}
