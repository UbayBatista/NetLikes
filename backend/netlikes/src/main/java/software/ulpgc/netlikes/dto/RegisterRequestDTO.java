package software.ulpgc.netlikes.dto;

import software.ulpgc.netlikes.model.Genre;
import java.util.List;
import java.util.Date;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequestDTO {

    private String userName;
    private String email;
    private Date birthdate;
    private String password;
    private String securityQuestion;
    private String answer;
    private List<Genre> favoriteGenres;
}