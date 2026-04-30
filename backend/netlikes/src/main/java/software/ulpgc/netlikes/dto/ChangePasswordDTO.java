package software.ulpgc.netlikes.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {
    private String email;
    private String newPassword;
}