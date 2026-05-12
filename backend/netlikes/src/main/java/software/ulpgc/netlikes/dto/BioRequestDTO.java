package software.ulpgc.netlikes.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BioRequestDTO {
    @Size(max = 60, message = "La bio no puede superar los 60 caracteres")
    private String bio;
}