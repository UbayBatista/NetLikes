package software.ulpgc.netlikes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyResponseDTO {
    private String senderEmail;
    private String senderName;
    private String senderProfilePicture;
    private String message;
    private Date date;
    private boolean read;
    private String type;
}