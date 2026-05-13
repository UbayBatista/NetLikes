package software.ulpgc.netlikes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.Mark;
import software.ulpgc.netlikes.model.MarkId;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.MarkRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkService {

    private final MarkRepository markRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ObjectMapper objectMapper;

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

    public Mark typeFilm(String email, Integer filmId, Mark.Type type) {
        MarkId id = new MarkId(email, filmId, type);

        return markRepository.findById(id)
            .map(relationExists -> {
                relationExists.setType(type);
                updateUserVector(relationExists.getUser(), relationExists.getFilm(), type);
                return markRepository.save(relationExists);
            })
            .orElseGet(() -> {
                User user = userRepository.findById(email)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                Film film = filmRepository.findById(filmId)
                        .orElseThrow(() -> new IllegalArgumentException("Película no encontrada"));

                Mark relation = new Mark();
                relation.setId(id);
                relation.setUser(user);
                relation.setFilm(film);
                relation.setType(type);

                updateUserVector(user, film, type);

                return markRepository.save(relation);
            });
    }

    private void updateUserVector(User user, Film film, Mark.Type type) {
        try {
            if (user.getVector() == null || film.getVector() == null) {
                return;
            }

            double filmWeight = (type == Mark.Type.SEEN) ? 0.15 : 0.05;
            double userWeight = 1.0 - filmWeight;

            double[] userVector = objectMapper.readValue(user.getVector(), double[].class);
            double[] filmVector = objectMapper.readValue(film.getVector(), double[].class);

            for (int i = 0; i < userVector.length; i++) {
                userVector[i] = (userVector[i] * userWeight) + (filmVector[i] * filmWeight);
            }

            user.setVector(objectMapper.writeValueAsString(userVector));
            userRepository.save(user);
            
            log.info("Vector actualizado para {}. Tipo: {}. Peso película: {}%", 
                     user.getEmail(), type, filmWeight * 100);

        } catch (Exception e) {
            log.error("Error matemático al recalcular el vector del usuario", e);
        }
    }

    public void deletetype(String email, Integer filmId, Mark.Type type) {
        markRepository.deleteByUserEmailAndFilmIdAndType(email, filmId, type);
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