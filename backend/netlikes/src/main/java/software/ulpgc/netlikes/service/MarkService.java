package software.ulpgc.netlikes.service;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.MarkRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.dto.FilmResponseDTO;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;

@Service
@RequiredArgsConstructor
public class MarkService {

    private final MarkRepository markRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;

    @Transactional 
    public String toggleMarkLogic(String email, Integer filmId, Mark.Type newType) {
        User user = userRepository.findById(email).orElseThrow();
        Film film = filmRepository.findById(filmId).orElseThrow();

        boolean alreadyExists = markRepository.existsByUserEmailAndFilmIdAndType(email, filmId, newType);

        if (alreadyExists) {
            markRepository.deleteByUserEmailAndFilmIdAndType(email, filmId, newType);
            return "removed";
        } else {
            if (newType == Mark.Type.WATCHLATER) {
                markRepository.deleteByUserEmailAndFilmIdAndType(email, filmId, Mark.Type.SEEN);
            } else if (newType == Mark.Type.SEEN) {
                markRepository.deleteByUserEmailAndFilmIdAndType(email, filmId, Mark.Type.WATCHLATER);
            }
            saveMark(user, film, newType);
            return "added";
        }
    }

    private void saveMark(User user, Film film, Mark.Type type) {
        Mark mark = new Mark();
        mark.setUser(user);
        mark.setFilm(film);
        mark.setType(type);
        markRepository.save(mark);
    }

    public List<Mark.Type> getMarkTypesForFilm(String email, Integer filmId) {
        return markRepository.findTypesByUserEmailAndFilmId(email, filmId);
    }

    public List<FilmResponseDTO> getFilmsByMarkType(String email, Mark.Type type) {
        return markRepository.findFilmsByUserEmailAndType(email, type).stream() 
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