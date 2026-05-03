package software.ulpgc.netlikes.unit;

import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.service.FilmService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;



@ExtendWith(MockitoExtension.class)
public class FilmServiceUnitTest {

    @Mock
    private FilmRepository filmRepository;

    @InjectMocks
    private FilmService filmService;

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        when(filmRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            filmService.getFilmById(999);
        });
    }
}