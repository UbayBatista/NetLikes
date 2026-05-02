package software.ulpgc.netlikes.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.Participate;
import software.ulpgc.netlikes.model.ParticipateId;
import software.ulpgc.netlikes.model.Platform;
import software.ulpgc.netlikes.model.Video;
import software.ulpgc.netlikes.model.Actor;

import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.PlatformRepository;
import software.ulpgc.netlikes.repository.ActorRepository;
import software.ulpgc.netlikes.dto.CastDTO;
import software.ulpgc.netlikes.dto.FilmRequestDTO;
import software.ulpgc.netlikes.dto.FilmResponseDTO;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final ActorRepository actorRepository;

    public List<FilmResponseDTO> getAllFilms() {
        return this.filmRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ResponseEntity<List<FilmResponseDTO>> getAllFilms(int page, int size) {
        PageRequest paginacion = PageRequest.of(page, size);

        List<FilmResponseDTO> catalogo = filmRepository.findAll(paginacion).getContent().stream().map(this::toDTO).toList();
        
        return ResponseEntity.ok(catalogo);
    }

    public FilmResponseDTO getFilmById(Integer id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film not found"));
        return toDTO(film);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FilmResponseDTO saveFilm(FilmRequestDTO dto) {

        Film film = new Film();
        applyDtoToEntity(dto, film);

        filmRepository.save(film);
        return toDTO(film);
    }

    public FilmResponseDTO updateFilm(Integer id, FilmRequestDTO dto) {

        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film not found"));

        applyDtoToEntity(dto, film);

        filmRepository.save(film);
        return toDTO(film);
    }

    public void deleteFilm(Integer id) {
        filmRepository.deleteById(id);
    }

    public ResponseEntity<List<FilmResponseDTO>> searchBy(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        Pageable topTen = PageRequest.of(0, 10);
        List<FilmResponseDTO> results = filmRepository.findByTitleContainingIgnoreCase(query, topTen).stream().map(this::toDTO).toList();
        
        return ResponseEntity.ok(results);
    }

    private void applyDtoToEntity(FilmRequestDTO dto, Film film) {
        film.setId(dto.getId());
        film.setTitle(dto.getTitle());
        film.setOverView(dto.getOverView());
        film.setAdult(dto.isAdult());
        film.setAgeRating(dto.getAgeRating());
        film.setTagLine(dto.getTagline());
        film.setRuntime(dto.getRuntime());
        film.setReleaseDate(dto.getReleaseDate());
        film.setPosterPath(dto.getPosterPath());

        List<Genre> genres = dto.getGenres().entrySet().stream()
            .map(entry -> {
                Integer genreId = Integer.valueOf(entry.getKey().toString());
                
                return genreRepository.findById(genreId)
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setId(genreId);
                        newGenre.setName(entry.getValue());
                        return genreRepository.save(newGenre);
                    });
            })
            .toList();

        film.setGenres(genres);

        List<Platform> platforms = dto.getWatchProviders().stream()
            .map(entry -> platformRepository.findById(Integer.parseInt(entry.get("id")))
                .orElseGet(() -> {
                    Platform newPlatform = new Platform();
                    newPlatform.setId(Integer.parseInt(entry.get("id")));
                    newPlatform.setName(entry.get("name"));
                    newPlatform.setLogo(entry.get("logoPath"));
                    return platformRepository.save(newPlatform);
                })
            )
            .toList();
        film.setWatchProviders(platforms);

        Set<Participate> participations = dto.getCast().stream()
            .map(entry -> {
                Actor actor = actorRepository.findById(Integer.parseInt(entry.get("id")))
                .orElseGet(() -> {
                    Actor newActor = new Actor();
                    newActor.setId(Integer.parseInt(entry.get("id")));
                    newActor.setName(entry.get("name"));
                    newActor.setProfilePath(entry.get("profilePath"));
                    return actorRepository.save(newActor);
                });
                Participate participation = new Participate();
                participation.setActor(actor);
                participation.setFilm(film);
                participation.setCharacter(entry.get("character"));
                ParticipateId participateId = new ParticipateId();
                participateId.setActorId(actor.getId());
                participateId.setCharacter(entry.get("character"));
                participateId.setFilmId(film.getId());
                participation.setParticipateId(participateId);
                return participation;
            })
            .collect(Collectors.toSet());
        film.setCast(participations);

        List<Video> videos = dto.getVideos().stream()
            .map(entry -> {
                Video video = new Video();
                video.setId(entry.get("id"));
                video.setKey(entry.get("key"));
                video.setType(entry.get("type"));
                video.setFilm(film);
                return video;
            })
            .toList();
        film.setVideos(videos);
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
