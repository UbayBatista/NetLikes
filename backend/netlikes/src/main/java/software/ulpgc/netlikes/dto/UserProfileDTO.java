package software.ulpgc.netlikes.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileDTO {

    private String email;
    private String userName;
    private String bio;
    @JsonProperty("isPrivate")
    private Boolean isPrivate;
    private int followers;
    private int following;
    private List<FilmResponseDTO> watchedFilms;
    private List<FilmResponseDTO> laterFilms;
}