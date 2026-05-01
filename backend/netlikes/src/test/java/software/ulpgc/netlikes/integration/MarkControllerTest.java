package software.ulpgc.netlikes.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.model.*;
import software.ulpgc.netlikes.repository.*;

import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test"
    }
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MarkControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private FilmRepository filmRepository;
    @Autowired private MarkRepository markRepository;

    @BeforeEach
    void setUp() {
        User user = new User(); user.setEmail("test@test.com"); user.setPassword("123"); user.setUserName("test");
        userRepository.save(user);

        Film film = new Film(); film.setId(1); film.setTitle("Matrix");
        filmRepository.save(film);
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
        Mark mark = new Mark(); 
        mark.setId(new MarkId("test@test.com", 1));
        mark.setUser(userRepository.findById("test@test.com").get());
        mark.setFilm(filmRepository.findById(1).get());
        mark.setType(Mark.Type.SEEN);
        markRepository.save(mark);

        mockMvc.perform(post("/marks/test@test.com/toggle/1")
                .param("type", "SEEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("removed"));
    }
}
