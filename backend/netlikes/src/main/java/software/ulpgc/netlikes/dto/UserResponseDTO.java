package software.ulpgc.netlikes.dto;

import lombok.*;

@Getter
@Setter
public class UserResponseDTO {
    private String email;
    private String userName;
    private String profilePicture;

    public UserResponseDTO(){}

    public UserResponseDTO(String email, String userName, String profilePicture){
        this.email = email;
        this.userName = userName;
        this.profilePicture = profilePicture;
    }

    
}
