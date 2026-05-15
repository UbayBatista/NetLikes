package software.ulpgc.netlikes.dto;

import lombok.Data;
import java.sql.Date;
import java.util.Map;
import java.util.List;

@Data
public class FilmRequestDTO {
    
    private Integer id;
    private String title;
    private String overView;
    private boolean adult;
    private String ageRating;
    private String tagline;
    private Integer runtime;
    private Date releaseDate;
    private String posterPath;

    private Map<Integer, String> genres;
    private List<Map<String, String>> cast;
    private List<Map<String, String>> videos;
    private List<Map<String, String>> watchProviders;

    private String vector;
}
