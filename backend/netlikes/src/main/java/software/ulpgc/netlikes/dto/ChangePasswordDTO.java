package software.ulpgc.netlikes.dto;

import org.springframework.lang.NonNull;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {
    @NonNull private String email;
    private String newPassword;
}