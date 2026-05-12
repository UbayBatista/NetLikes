package software.ulpgc.netlikes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Video;
import software.ulpgc.netlikes.dto.CastDTO;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public List<FilmResponseDTO> getRecommendationsForUser(String userEmail) {
        User user = userRepository.findById(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String userVector = user.getVector();
        if (userVector == null || userVector.isEmpty()) {
            throw new RuntimeException("El usuario aún no tiene gustos configurados");
        }

        return filmRepository.findTop50Recommendations(userEmail, userVector).stream()
            .map(this::toDTO)
            .toList();
    }

    private FilmResponseDTO toDTO(Film film) {

        FilmResponseDTO dto = new FilmResponseDTO();

        dto.setId(film.getId());
        dto.setAdult(film.isAdult());
        dto.setTitle(film.getTitle());
        dto.setPosterPath(film.getPosterPath());
        dto.setOverView(film.getOverView());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setRuntime(film.getRuntime());
        dto.setAgeRating(film.getAgeRating());
        dto.setTagLine(film.getTagLine());
        
        dto.setGenres(film.getGenres().stream().map(Genre::getName).toList());
        dto.setWatchProviders(film.getWatchProviders());
        dto.setCast(film.getCast()
        .stream()
        .map(participate -> {
            CastDTO cast = new CastDTO();
            cast.setCharacter(participate.getCharacter());
            cast.setName(participate.getActor().getName());
            cast.setProfilePath(participate.getActor().getProfilePath());
            return cast;
        })
        .toList());
        dto.setVideos(film.getVideos().stream().map(Video::getKey).toList());

        return dto;
    }
}