package software.ulpgc.netlikes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import software.ulpgc.netlikes.model.Film;

@Data
@AllArgsConstructor
public class RecommendCountDTO {
    private Film film;
    private Long count;
}