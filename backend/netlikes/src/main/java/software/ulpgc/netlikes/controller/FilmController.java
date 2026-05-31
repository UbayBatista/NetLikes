package software.ulpgc.netlikes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

import software.ulpgc.netlikes.service.FilmService;

import software.ulpgc.netlikes.dto.FilmResponseDTO;


@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<FilmResponseDTO>> getAllFilms(
        @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "100") int size
    ) {
        return filmService.getAllFilms(page, size);
    }

    @GetMapping("/{id}")
    public FilmResponseDTO getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Integer id) {
        filmService.deleteFilm(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmResponseDTO>> searchFilm(@RequestParam String query) {
        return this.filmService.searchBy(query);
    }


}
