package software.ulpgc.netlikes.integration;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class RecommendControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private FilmRepository filmRepository;

    private void createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setName("User_" + email);
        user.setPassword("123");
        user.setSecurityQuestion("Q");
        user.setAnswer("A");
        user.setBirthdate(new Date());
        user.setVector("");
        userRepository.save(user);
    }

    private void createFilm(Integer id) {
        Film film = new Film();
        film.setId(id);
        film.setTitle("Test Film " + id);
        film.setOverView("Test");
        film.setPosterPath("path");
        film.setVector("");
        filmRepository.save(film);
    }

    @BeforeEach
    void setUp() {
        this.createUser("sender@test.com");
        this.createUser("receiver@test.com");
        this.createFilm(1);
    }

    @Test
    @DisplayName("Should return Ok and send multiple recommendations when valid payload is provided")
    void should_ReturnOkandsendMultipleRecommendationsSuccessfully_when_validPayloadIsProvided() throws Exception {
        String requestBody = """
            {
                "filmId": 1,
                "targetEmails": ["receiver@test.com"]
            }
            """;

        mockMvc.perform(post("/api/recommend/bulk")
                .header("X-User-Id", "sender@test.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Recomendaciones enviadas"));
    }

    @Test
    @DisplayName("Should return Ok and a list of recent recommendations when a valid user ID header is provided")
    void should_ReturnOkandreturnRecentRecommendationsList_when_validUserIdHeaderIsProvided() throws Exception {
        mockMvc.perform(get("/api/recommend/recent")
                .header("X-User-Id", "sender@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}