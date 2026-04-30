package software.ulpgc.netlikes.service;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.model.MarkId;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.MarkRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.dto.FilmResponseDTO;


import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.*;

@Service
@RequiredArgsConstructor
public class MarkService {

    private final MarkRepository markRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    
    public Mark typeFilm(String email, Integer filmId, Mark.Type type) {
    MarkId id = new MarkId(email, filmId);

    return markRepository.findById(id)
        .map(relationExists -> {
            relationExists.setType(type);
            return markRepository.save(relationExists);
        })
        .orElseGet(() -> {
            User user = userRepository.findById(email).orElseThrow();
            Film film = filmRepository.findById(filmId).orElseThrow();

            Mark relation = new Mark();
            relation.setId(id); 
            relation.setUser(user);
            relation.setFilm(film);
            relation.setType(type);

            return markRepository.save(relation);
        });
    }

    public void deletetype(String email, Integer filmId) {
        MarkId id = new MarkId(email, filmId);
        markRepository.deleteById(id);
    }

    public boolean exists(String email, Integer filmId) {
        return markRepository.existsById(new MarkId(email, filmId));
    }

    public Optional<Mark> getMark(String email, Integer filmId) {
        return markRepository.findById(new MarkId(email, filmId));
    }

    public List<FilmResponseDTO> getFilmsByMarkType(String email, Mark.Type type) {
        return markRepository.findAll().stream()
                .filter(m -> m.getUser().getEmail().equals(email) && m.getType() == type)
                .map(Mark::getFilm)
                .map(this::toFilmDTO)
                .toList();
    }

    private FilmResponseDTO toFilmDTO(Film film) {
        FilmResponseDTO dto = new FilmResponseDTO();
        dto.setId(film.getId());
        dto.setTitle(film.getTitle());
        dto.setPosterPath(film.getPosterPath());
        dto.setTagLine(film.getTagLine());
        dto.setOverView(film.getOverView());
        dto.setRuntime(film.getRuntime()); 
        dto.setAgeRating(film.getAgeRating());
        dto.setAdult(film.isAdult());
        dto.setReleaseDate(film.getReleaseDate());
        return dto;
    }
}
