package software.ulpgc.netlikes.dto;

import lombok.Data;
import software.ulpgc.netlikes.model.Platform;

import java.sql.Date;
import java.util.List;

@Data
public class FilmResponseDTO {

    private Integer id;
    private String title;
    private String posterPath;
    private String tagLine;
    private String overView;
    private Integer runtime;
    private String ageRating;
    private boolean adult;
    private Date releaseDate;
    
    private List<String> genres;
    private List<CastDTO> cast;
    private List<String> videos;
    private List<Platform> watchProviders;
}

