package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.Film;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FilmRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional 
@ActiveProfiles("test")
public class RecommendationIT {

    @Autowired
    private FilmRepository filmRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return top recommendations ordered by closest vector distance when queried with user vector")
    void should_ReturnRecommendationsOrderedByVectorDistance_when_queriedWithUserVector() {
        String userEmail = "test_recs@email.com";
        String userVector = createVector(1.0);

        userRepository.save(createUser(userEmail, userVector));

        saveFilm(1001, "Peli Opuesta", createVector(-1.0));
        saveFilm(1002, "Peli Perfecta", createVector(0.9));
        saveFilm(1003, "Peli Mala", createVector(0.0));
        saveFilm(1004, "Peli Aceptable", createVector(0.5));

        List<Film> recommendations = filmRepository.findTop50Recommendations(userEmail, userVector);
        List<Film> testFilms = recommendations.stream()
                .filter(film -> film.getTitle().startsWith("Peli "))
                .toList();

        assertEquals(4, testFilms.size(), "Debería traer las 4 películas de prueba");
        assertEquals(List.of("Peli Perfecta", "Peli Aceptable", "Peli Mala", "Peli Opuesta"),
                testFilms.stream().map(Film::getTitle).toList());
    }

    private User createUser(String email, String vector) {
        User user = new User();
        user.setEmail(email);
        user.setName("Test User");
        user.setBirthdate(new java.util.GregorianCalendar(2000, 0, 1).getTime());
        user.setSecurityQuestion("País");
        user.setAnswer("España");
        user.setPassword("password");
        user.setVector(vector);
        return user;
    }

    private void saveFilm(int id, String title, String vector) {
        Film film = new Film();
        film.setId(id);
        film.setTitle(title);
        film.setPosterPath("url/para/la/prueba");
        film.setVector(vector);
        film.setOverView("Descripción de prueba");
        filmRepository.save(film);
    }

    private String createVector(double firstValue) {
        double[] vector = new double[384];
        vector[0] = firstValue;
        return Arrays.toString(vector);
    }
}