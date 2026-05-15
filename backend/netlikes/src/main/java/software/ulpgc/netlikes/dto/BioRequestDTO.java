package software.ulpgc.netlikes.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BioRequestDTO {
    @Size(max = 120, message = "La bio no puede superar los 120 caracteres")
    private String bio;
}