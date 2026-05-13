package software.ulpgc.netlikes.integration;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MarkControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private FilmRepository filmRepository;
    @Autowired private MarkRepository markRepository;

    private User savedUser;
    private Film savedFilm;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("123");
        user.setName("test");
        user.setSecurityQuestion("Q");
        user.setAnswer("A");
        user.setBirthdate(java.sql.Date.valueOf("2000-01-01"));
        savedUser = userRepository.save(user);

        Film film = new Film();
        film.setId(1);
        film.setTitle("Matrix");
        film.setOverView("Test");
        film.setPosterPath("path");
        savedFilm = filmRepository.save(film);
    }

    private Mark createAndSaveMark(Mark.Type type) {
        Mark mark = new Mark();
        mark.setUser(savedUser);
        mark.setFilm(savedFilm);
        mark.setType(type);
        return markRepository.save(mark);
    }

    @Test
    void toggleMark_AddSeenList_ReturnsAdded() throws Exception {
        mockMvc.perform(post("/marks/test@test.com/toggle/1")
            .param("type", "SEEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("added"));
    }

    @Test
    void toggleMark_AlreadySeen_ReturnsRemoved() throws Exception {
        createAndSaveMark(Mark.Type.SEEN);

        mockMvc.perform(post("/marks/test@test.com/toggle/1")
            .param("type", "SEEN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("removed"));
    }
}