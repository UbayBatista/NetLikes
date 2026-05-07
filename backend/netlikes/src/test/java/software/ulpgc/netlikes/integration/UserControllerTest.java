package software.ulpgc.netlikes.integration;

import software.ulpgc.netlikes.dto.ChangePasswordDTO;
import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.ValidAnswerRequestDTO;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;

import java.sql.Date;
import java.util.List;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Genre savedGenre1;
    private Genre savedGenre2;
    private Genre savedGenre3;

    @BeforeEach
    void setUp() {
        Genre g1 = new Genre(); g1.setId(21); g1.setName("Acción");
        Genre g2 = new Genre(); g2.setId(22); g2.setName("Comedia");
        Genre g3 = new Genre(); g3.setId(23); g3.setName("Drama");
        savedGenre1 = genreRepository.save(g1);
        savedGenre2 = genreRepository.save(g2);
        savedGenre3 = genreRepository.save(g3);
    }

    private User createAndSaveUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("1234"));
        user.setBirthdate(Date.valueOf("1900-05-21"));
        user.setName("Juan");
        user.setSecurityQuestion("¿Nombre de tu mascota?");
        user.setAnswer("Toby");
        user.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        return userRepository.save(user);
    }

    @Test
    void register_shouldReturn200_whenDataIsValid() throws Exception {

        RegisterRequestDTO request = new RegisterRequestDTO(
            "Juan",
            "juan@email.com",
            Date.valueOf("1900-05-21"),
            "1234",
            "¿Nombre de tu primera mascota?",
            "Toby",
            List.of(savedGenre1, savedGenre2, savedGenre3)
        );

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@email.com"))
            .andExpect(jsonPath("$.userName").value("Juan"));
    }

    @Test
    void register_shouldReturn400_whenNameAlreadyExists() throws Exception {
        createAndSaveUser("juan_original@email.com");

        RegisterRequestDTO request = new RegisterRequestDTO(
            "Juan", "nuevo_correo@email.com", Date.valueOf("1900-05-21"), "1234", "¿Mascota?", "Toby", List.of(savedGenre1)
        );

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenEmailAlreadyExists() throws Exception {
        createAndSaveUser("juan@email.com");

        RegisterRequestDTO request = new RegisterRequestDTO(
            "Juan",
            "juan@email.com",
            Date.valueOf("1900-05-21"),
            "1234",
            "¿Nombre de tu primera mascota?",
            "Toby",
            List.of(savedGenre1, savedGenre2, savedGenre3)
        );

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn200_whenCredentialsAreCorrect() throws Exception {
        createAndSaveUser("juan@email.com");

        LoginRequestDTO request = new LoginRequestDTO("juan@email.com", "1234");

        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void login_shouldReturn401_whenPasswordIsWrong() throws Exception {
        createAndSaveUser("juan@email.com");

        LoginRequestDTO request = new LoginRequestDTO("juan@email.com", "1222");

        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401_whenUserNotFound() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("fantasma@email.com", "1234");

        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void existsEmail_shouldReturn200True_whenEmailExists() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/exists/juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void existsEmail_shouldReturn200False_whenEmailNotExists() throws Exception {
        mockMvc.perform(get("/users/exists/juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    void existsName_shouldReturn200True_whenNameExists() throws Exception {
        createAndSaveUser("juan@email.com");
        mockMvc.perform(get("/users/existsName/Juan"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void existsName_shouldReturn200False_whenNameNotExists() throws Exception {
        mockMvc.perform(get("/users/existsName/FalsoUser"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    void getSecurityQuestion_shouldReturn200_whenUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/securityQuestion/juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("¿Nombre de tu mascota?"));
    }

    @Test
    void getSecurityQuestion_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/users/securityQuestion/juan@email.com"))
            .andExpect(status().isNotFound());
    }   

    @Test
    void isValidAnswer_shouldReturn200True_whenAnswerIsCorrect() throws Exception {
        createAndSaveUser("juan@email.com");

        ValidAnswerRequestDTO request = new ValidAnswerRequestDTO("juan@email.com", "Toby");

        mockMvc.perform(post("/users/isValidAnswer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void isValidAnswer_shouldReturn200False_whenAnswerIsWrong() throws Exception {
        createAndSaveUser("juan@email.com");

        ValidAnswerRequestDTO request = new ValidAnswerRequestDTO("juan@email.com", "RespuestaErronea");

        mockMvc.perform(post("/users/isValidAnswer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    void changePassword_shouldReturn200_whenUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        ChangePasswordDTO request = new ChangePasswordDTO("juan@email.com", "nueva1234");

        mockMvc.perform(patch("/users/changePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
                
        User updatedUser = userRepository.findById("juan@email.com").get();
        assertTrue(passwordEncoder.matches("nueva1234", updatedUser.getPassword()));
    }
    
    @Test
    void userProfile_shouldReturn200_whenAccountIsPublic() throws Exception {
        User requester = new User();
        requester.setEmail("yo@email.com");
        requester.setPassword(passwordEncoder.encode("1234"));
        requester.setBirthdate(Date.valueOf("1990-01-01"));
        requester.setName("Yo");
        requester.setSecurityQuestion("?");
        requester.setAnswer("x");
        requester.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        userRepository.save(requester);

        User target = new User();
        target.setEmail("ana@email.com");
        target.setPassword(passwordEncoder.encode("1234"));
        target.setBirthdate(Date.valueOf("1995-03-10"));
        target.setName("Ana");
        target.setSecurityQuestion("?");
        target.setAnswer("x");
        target.setAccountPrivacity(false);
        target.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        userRepository.save(target);

        mockMvc.perform(get("/users/profile/Ana")
                .param("requesterEmail", "yo@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@email.com"))
            .andExpect(jsonPath("$.userName").value("Ana"))
            .andExpect(jsonPath("$.isPrivate").value(false))
            .andExpect(jsonPath("$.watchedFilms").isArray())
            .andExpect(jsonPath("$.laterFilms").isArray());
    }

    @Test
    void userProfile_shouldReturn200_withNullLists_whenPrivateAndNotFollowing() throws Exception {
        User requester = new User();
        requester.setEmail("yo@email.com");
        requester.setPassword(passwordEncoder.encode("1234"));
        requester.setBirthdate(Date.valueOf("1990-01-01"));
        requester.setName("Yo");
        requester.setSecurityQuestion("?");
        requester.setAnswer("x");
        requester.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        userRepository.save(requester);

        User target = new User();
        target.setEmail("ana@email.com");
        target.setPassword(passwordEncoder.encode("1234"));
        target.setBirthdate(Date.valueOf("1995-03-10"));
        target.setName("Ana");
        target.setSecurityQuestion("?");
        target.setAnswer("x");
        target.setAccountPrivacity(true);
        target.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        userRepository.save(target);

        mockMvc.perform(get("/users/profile/Ana")
                .param("requesterEmail", "yo@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isPrivate").value(true))
            .andExpect(jsonPath("$.watchedFilms").doesNotExist())
            .andExpect(jsonPath("$.laterFilms").doesNotExist());
    }

    @Test
    void userProfile_shouldReturn200_withContent_whenViewingOwnProfile() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/profile/Juan")
                .param("requesterEmail", "juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@email.com"))
            .andExpect(jsonPath("$.watchedFilms").isArray())
            .andExpect(jsonPath("$.laterFilms").isArray());
    }

    @Test
    void searchBy_shouldReturn200_withMatchingUsers() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/search")
                .param("query", "Juan"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userName").value("Juan"));
    }

    @Test
    void searchBy_shouldReturn200_withEmptyList_whenNoMatches() throws Exception {
        mockMvc.perform(get("/users/search")
                .param("query", "UsuarioQueNoExiste"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchBy_shouldReturn200_withEmptyList_whenQueryIsBlank() throws Exception {
        mockMvc.perform(get("/users/search")
                .param("query", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void changePrivacy_shouldReturn200_whenSetToPrivate() throws Exception {
        createAndSaveUser("juan@email.com");

        String body = "{\"isPrivate\": true}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/privacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        User updated = userRepository.findById("juan@email.com").get();
        assertTrue(updated.isAccountPrivacity());
    }

    @Test
    void changePrivacy_shouldReturn200_whenSetToPublic() throws Exception {
        User user = createAndSaveUser("juan@email.com");
        user.setAccountPrivacity(true);
        userRepository.save(user);

        String body = "{\"isPrivate\": false}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/privacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        User updated = userRepository.findById("juan@email.com").get();
        assertFalse(updated.isAccountPrivacity());
    }

    @Test
    void userProfile_shouldHideListsWhenPrivateAndNotFollowing() throws Exception {
        User target = new User();
        target.setEmail("privado@email.com");
        target.setPassword(passwordEncoder.encode("1234"));
        target.setBirthdate(Date.valueOf("1995-03-10"));
        target.setName("Privado");
        target.setSecurityQuestion("?");
        target.setAnswer("x");
        target.setAccountPrivacity(true);
        target.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        userRepository.save(target);

        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/profile/Privado")
                .param("requesterEmail", "juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value("Privado"))
            .andExpect(jsonPath("$.watchedFilms").doesNotExist())
            .andExpect(jsonPath("$.watchLaterFilms").doesNotExist());
    }

    @Test
    void userProfile_shouldShowListsWhenPublicAndNotFollowing() throws Exception {
        User target = new User();
        target.setEmail("publico@email.com");
        target.setPassword(passwordEncoder.encode("1234"));
        target.setBirthdate(Date.valueOf("1995-03-10"));
        target.setName("Publico");
        target.setSecurityQuestion("?");
        target.setAnswer("x");
        target.setAccountPrivacity(false);
        target.setFavoriteGenres(List.of(savedGenre1, savedGenre2, savedGenre3));
        userRepository.save(target);

        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/profile/Publico")
                .param("requesterEmail", "juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value("Publico"))
            .andExpect(jsonPath("$.watchedFilms").isArray())
            .andExpect(jsonPath("$.laterFilms").isArray());
    }

    @Test
    void deleteUser_shouldReturn200_whenUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(delete("/users")
            .header("X-User-Id", "juan@email.com"))
            .andExpect(status().isOk());

        assertFalse(userRepository.existsById("juan@email.com"));
    }

    @Test
    void deleteUser_shouldReturn500_whenUserNotFound() throws Exception {
        assertThrows(ServletException.class, () ->
            mockMvc.perform(delete("/users")
                .header("X-User-Id", "noexiste@email.com"))
                .andReturn()
        );
    }
}