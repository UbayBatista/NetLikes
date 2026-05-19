package software.ulpgc.netlikes.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VisibilityRequestDTO {
    @JsonProperty("isVisible")
    private Boolean isVisible;
}