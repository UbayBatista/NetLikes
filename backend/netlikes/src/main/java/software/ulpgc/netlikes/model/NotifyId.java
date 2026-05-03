package software.ulpgc.netlikes.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyId {
    private String emailSender;
    private String emailReceiver;
    private String message;
}
