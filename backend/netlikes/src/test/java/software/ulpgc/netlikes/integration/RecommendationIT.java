package software.ulpgc.netlikes.integration;

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
class RecommendationIT {

    @Autowired
    private FilmRepository filmRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindTop50Recommendations_OrdersByVectorDistance() {
        String userEmail = "test_recs@email.com";
        User user = new User();
        user.setEmail(userEmail);
        user.setName("Test User");
        user.setBirthdate(new java.util.GregorianCalendar(2000, 0, 1).getTime());
        user.setSecurityQuestion("País");
        user.setAnswer("España");
        user.setPassword("password");
        String userVector = createVector(1.0); 
        user.setVector(userVector);
        userRepository.save(user);

        Film filmOpposite = new Film();
        filmOpposite.setId(1001);
        filmOpposite.setTitle("Peli Opuesta");
        filmOpposite.setPosterPath("url/para/la/prueba");
        filmOpposite.setVector(createVector(-1.0));
        filmOpposite.setOverView("Descripción de prueba");
        filmRepository.save(filmOpposite);

        Film filmPerfect = new Film();
        filmPerfect.setId(1002);
        filmPerfect.setOverView("Descripción de prueba");
        filmPerfect.setPosterPath("url/para/la/prueba");
        filmPerfect.setTitle("Peli Perfecta");
        filmPerfect.setVector(createVector(0.9)); 
        filmRepository.save(filmPerfect);

        Film filmBad = new Film();
        filmBad.setId(1003);
        filmBad.setOverView("Descripción de prueba");
        filmBad.setPosterPath("url/para/la/prueba");
        filmBad.setTitle("Peli Mala");
        filmBad.setVector(createVector(0.0));
        filmRepository.save(filmBad);

        Film filmOk = new Film();
        filmOk.setId(1004);
        filmOk.setOverView("Descripción de prueba");
        filmOk.setPosterPath("url/para/la/prueba");
        filmOk.setTitle("Peli Aceptable");
        filmOk.setVector(createVector(0.5)); 
        filmRepository.save(filmOk);

        List<Film> recommendations = filmRepository.findTop50Recommendations(userEmail, userVector);

        List<Film> testFilms = recommendations.stream()
                .filter(f -> f.getTitle().startsWith("Peli "))
                .toList();

        assertEquals(4, testFilms.size(), "Debería traer las 4 películas de prueba");

        assertEquals("Peli Perfecta", testFilms.get(0).getTitle());
        assertEquals("Peli Aceptable", testFilms.get(1).getTitle());
        assertEquals("Peli Mala", testFilms.get(2).getTitle());
        assertEquals("Peli Opuesta", testFilms.get(3).getTitle());
        
        System.out.println("¡Prueba superada! PostgreSQL está ordenando los vectores correctamente.");
    }

    private String createVector(double firstValue) {
        double[] vec = new double[384];
        vec[0] = firstValue;
        return Arrays.toString(vec);
    }
}