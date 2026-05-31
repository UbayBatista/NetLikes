package software.ulpgc.netlikes.integration;

import software.ulpgc.netlikes.dto.ChangePasswordDTO;
import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.ValidAnswerRequestDTO;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class UserControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Genre actionGenre;
    private Genre comedyGenre;
    private Genre dramaGenre;

    @BeforeEach
    void setUp() {
        Genre actionGenreData = new Genre(); actionGenreData.setId(21); actionGenreData.setName("Acción");
        Genre comedyGenreData = new Genre(); comedyGenreData.setId(22); comedyGenreData.setName("Comedia");
        Genre dramaGenreData = new Genre(); dramaGenreData.setId(23); dramaGenreData.setName("Drama");
        actionGenre = genreRepository.save(actionGenreData);
        comedyGenre = genreRepository.save(comedyGenreData);
        dramaGenre = genreRepository.save(dramaGenreData);
    }

    private User createAndSaveUser(String email) {
        return createAndSaveUser(email, "Juan", "1900-05-21", "1234", false);
    }

    private User createAndSaveUser(String email, String name, String birthdate, String password, boolean isPrivate) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setBirthdate(Date.valueOf(birthdate));
        newUser.setName(name);
        newUser.setSecurityQuestion("¿Nombre de tu mascota?");
        newUser.setAnswer("Toby");
        newUser.setVector("");
        newUser.setAccountPrivacity(isPrivate);
        newUser.setFavoriteGenres(List.of(actionGenre, comedyGenre, dramaGenre));
        return userRepository.save(newUser);
    }

    private User performPatchAndGetUpdatedUser(String email, String endpoint, String requestPayload) throws Exception {
        mockMvc.perform(patch(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPayload))
            .andExpect(status().isOk());
        return userRepository.findById(email).get();
    }

    @Test
    @DisplayName("Should return 200 when register data is valid")
    void should_Return200_when_RegisterDataIsValid() throws Exception {

        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
            "Juan",
            "juan@email.com",
            Date.valueOf("1900-05-21"),
            "1234",
            "¿Nombre de tu primera mascota?",
            "Toby",
            List.of(actionGenre, comedyGenre, dramaGenre)
        );

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@email.com"))
            .andExpect(jsonPath("$.userName").value("Juan"));
    }

    @Test
    @DisplayName("Should return 400 when name already exists")
    void should_Return400_when_NameAlreadyExists() throws Exception {
        createAndSaveUser("juan_original@email.com");

        RegisterRequestDTO duplicateNameRequest = new RegisterRequestDTO(
            "Juan", "nuevo_correo@email.com", Date.valueOf("1900-05-21"), "1234", "¿Mascota?", "Toby", List.of(actionGenre)
        );

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateNameRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when email already exists")
    void should_Return400_when_EmailAlreadyExists() throws Exception {
        createAndSaveUser("juan@email.com");

        RegisterRequestDTO duplicateEmailRequest = new RegisterRequestDTO(
            "Juan",
            "juan@email.com",
            Date.valueOf("1900-05-21"),
            "1234",
            "¿Nombre de tu primera mascota?",
            "Toby",
            List.of(actionGenre, comedyGenre, dramaGenre)
        );

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 when login credentials are correct")
    void should_Return200_when_CredentialsAreCorrect() throws Exception {
        createAndSaveUser("juan@email.com");

        LoginRequestDTO loginRequest = new LoginRequestDTO("juan@email.com", "1234");

        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    @DisplayName("Should return 401 when password is wrong")
    void should_Return401_when_PasswordIsWrong() throws Exception {
        createAndSaveUser("juan@email.com");

        LoginRequestDTO invalidPasswordRequest = new LoginRequestDTO("juan@email.com", "1222");

        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidPasswordRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when user not found")
    void should_Return401_when_UserNotFound() throws Exception {
        LoginRequestDTO nonExistentUserRequest = new LoginRequestDTO("fantasma@email.com", "1234");

        mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nonExistentUserRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 200 with true when email exists")
    void should_Return200True_when_EmailExists() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/exists/juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return 200 with false when email not exists")
    void should_Return200False_when_EmailNotExists() throws Exception {
        mockMvc.perform(get("/users/exists/juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should return 200 with true when name exists")
    void should_Return200True_when_NameExists() throws Exception {
        createAndSaveUser("juan@email.com");
        mockMvc.perform(get("/users/existsName/Juan"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return 200 with false when name not exists")
    void should_Return200False_when_NameNotExists() throws Exception {
        mockMvc.perform(get("/users/existsName/FalsoUser"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should return 200 when getting security question for existing user")
    void should_Return200_when_GetSecurityQuestionUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/securityQuestion/juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("¿Nombre de tu mascota?"));
    }

    @Test
    @DisplayName("Should return 404 when getting security question for non-existent user")
    void should_Return404_when_GetSecurityQuestionUserNotFound() throws Exception {
        mockMvc.perform(get("/users/securityQuestion/juan@email.com"))
            .andExpect(status().isNotFound());
    }   

    @Test
    @DisplayName("Should return 200 with true when security answer is correct")
    void should_Return200True_when_AnswerIsCorrect() throws Exception {
        createAndSaveUser("juan@email.com");

        ValidAnswerRequestDTO validAnswerRequest = new ValidAnswerRequestDTO("juan@email.com", "Toby");

        mockMvc.perform(post("/users/isValidAnswer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validAnswerRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return 200 with false when security answer is wrong")
    void should_Return200False_when_AnswerIsWrong() throws Exception {
        createAndSaveUser("juan@email.com");

        ValidAnswerRequestDTO invalidAnswerRequest = new ValidAnswerRequestDTO("juan@email.com", "RespuestaErronea");

        mockMvc.perform(post("/users/isValidAnswer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidAnswerRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should return 200 when changing password for existing user")
    void should_Return200_when_ChangePasswordUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        ChangePasswordDTO changePasswordRequest = new ChangePasswordDTO("juan@email.com", "nueva1234");

        mockMvc.perform(patch("/users/changePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());
                
        User updatedUser = userRepository.findById("juan@email.com").get();
        assertTrue(passwordEncoder.matches("nueva1234", updatedUser.getPassword()));
    }
    //--------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("Should return 200 when viewing public user profile")
    void should_Return200_when_AccountIsPublic() throws Exception {
        createAndSaveUser("yo@email.com", "Yo", "1990-01-01", "1234", false);
        createAndSaveUser("ana@email.com", "Ana", "1995-03-10", "1234", false);

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
    @DisplayName("Should return 200 with null lists when viewing private profile and not following")
    void should_Return200_withNullLists_when_PrivateAndNotFollowing() throws Exception {
        createAndSaveUser("yo@email.com", "Yo", "1990-01-01", "1234", false);
        createAndSaveUser("ana@email.com", "Ana", "1995-03-10", "1234", true);

        mockMvc.perform(get("/users/profile/Ana")
                .param("requesterEmail", "yo@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isPrivate").value(true))
            .andExpect(jsonPath("$.watchedFilms").doesNotExist())
            .andExpect(jsonPath("$.laterFilms").doesNotExist());
    }

    @Test
    @DisplayName("Should return 200 with content when viewing own profile")
    void should_Return200_withContent_when_ViewingOwnProfile() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/profile/Juan")
                .param("requesterEmail", "juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("juan@email.com"))
            .andExpect(jsonPath("$.watchedFilms").isArray())
            .andExpect(jsonPath("$.laterFilms").isArray());
    }

    @Test
    @DisplayName("Should return 200 with matching users when searching")
    void should_Return200_with_MatchingUsers() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/search")
                .param("query", "Juan"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userName").value("Juan"));
    }

    @Test
    @DisplayName("Should return 200 with empty list when no search matches found")
    void should_Return200_withEmptyList_when_NoMatches() throws Exception {
        mockMvc.perform(get("/users/search")
                .param("query", "UsuarioQueNoExiste"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return 200 with empty list when search query is blank")
    void should_Return200_withEmptyList_when_QueryIsBlank() throws Exception {
        mockMvc.perform(get("/users/search")
                .param("query", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should return 200 when setting privacy to private")
    void should_Return200_when_SetToPrivate() throws Exception {
        createAndSaveUser("juan@email.com");

        String privacyUpdatePayload = "{\"isPrivate\": true}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/privacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(privacyUpdatePayload))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findById("juan@email.com").get();
        assertTrue(updatedUser.isAccountPrivacity());
    }

    @Test
    @DisplayName("Should return 200 when setting privacy to public")
    void should_Return200_when_SetToPublic() throws Exception {
        User currentUser = createAndSaveUser("juan@email.com");
        currentUser.setAccountPrivacity(true);
        userRepository.save(currentUser);

        String privacyUpdatePayload = "{\"isPrivate\": false}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/privacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(privacyUpdatePayload))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findById("juan@email.com").get();
        assertFalse(updatedUser.isAccountPrivacity());
    }

    @Test
    @DisplayName("Should hide film lists when viewing private profile and not following")
    void should_HideLists_when_PrivateAndNotFollowing() throws Exception {
        createAndSaveUser("privado@email.com", "Privado", "1995-03-10", "1234", true);
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/profile/Privado")
                .param("requesterEmail", "juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value("Privado"))
            .andExpect(jsonPath("$.watchedFilms").doesNotExist())
            .andExpect(jsonPath("$.watchLaterFilms").doesNotExist());
    }

    @Test
    @DisplayName("Should show film lists when viewing public profile and not following")
    void should_ShowLists_when_PublicAndNotFollowing() throws Exception {
        createAndSaveUser("publico@email.com", "Publico", "1995-03-10", "1234", false);
        createAndSaveUser("juan@email.com");

        mockMvc.perform(get("/users/profile/Publico")
                .param("requesterEmail", "juan@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value("Publico"))
            .andExpect(jsonPath("$.watchedFilms").isArray())
            .andExpect(jsonPath("$.laterFilms").isArray());
    }

    @Test
    @DisplayName("Should return 200 when deleting existing user")
    void should_Return200_when_DeleteUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        mockMvc.perform(delete("/users")
            .header("X-User-Id", "juan@email.com"))
            .andExpect(status().isOk());

        assertFalse(userRepository.existsById("juan@email.com"));
    }

    @Test
    @DisplayName("Should return 500 when deleting non-existent user")
    void should_Return500_when_DeleteUserNotFound() throws Exception {
        assertThrows(ServletException.class, () ->
            mockMvc.perform(delete("/users")
                .header("X-User-Id", "noexiste@email.com"))
                .andReturn()
        );
    }

    @Test
    @DisplayName("Should return 200 when updating bio for existing user with valid bio")
    void should_Return200_when_UserExistsAndBioIsValid() throws Exception {
        createAndSaveUser("juan@email.com");

        String bioUpdatePayload = "{\"bio\": \"Esta es mi nueva bio\"}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/bio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioUpdatePayload))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findById("juan@email.com").get();
        assertTrue(updatedUser.getBio().equals("Esta es mi nueva bio"));
    }

    @Test
    @DisplayName("Should return 400 when bio exceeds 120 characters")
    void should_Return400_when_BioExceeds120Characters() throws Exception {
        createAndSaveUser("juan@email.com");

        String oversizeBio = "a".repeat(121);
        String bioUpdatePayload = "{\"bio\": \"" + oversizeBio + "\"}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/bio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioUpdatePayload))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 200 when bio is exactly 120 characters")
    void should_Return200_when_BioIsExactly120Characters() throws Exception {
        createAndSaveUser("juan@email.com");

        String bioWith120Characters = "a".repeat(120);
        String bioUpdatePayload = "{\"bio\": \"" + bioWith120Characters + "\"}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/bio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioUpdatePayload))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating bio for non-existent user")
    void should_Return404_when_UpdateBioUserNotFound() throws Exception {
        String bioUpdatePayload = "{\"bio\": \"Bio de nadie\"}";

        mockMvc.perform(patch("/users/myProfile/noexiste@email.com/bio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioUpdatePayload))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 when bio is empty")
    void should_Return200_when_BioIsEmpty() throws Exception {
        createAndSaveUser("juan@email.com");

        String bioUpdatePayload = "{\"bio\": \"\"}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/bio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioUpdatePayload))
            .andExpect(status().isOk());

        User updated = userRepository.findById("juan@email.com").get();
        assertTrue(updated.getBio().isEmpty());
    }

    @Test
    @DisplayName("Should return 200 when updating avatar for existing user")
    void should_Return200_when_UpdateAvatarUserExists() throws Exception {
        createAndSaveUser("juan@email.com");

        String avatarUpdatePayload = "{\"seed\": \"Moon\"}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(avatarUpdatePayload))
            .andExpect(status().isOk());

        User updated = userRepository.findById("juan@email.com").get();
        assertThat(updated.getProfilePicture()).isEqualTo("Moon");
    }

    @Test
    @DisplayName("Should return 404 when updating avatar for non-existent user")
    void should_Return404_when_UpdateAvatarUserNotFound() throws Exception {
        String avatarUpdatePayload = "{\"seed\": \"Moon\"}";

        mockMvc.perform(patch("/users/myProfile/noexiste@email.com/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(avatarUpdatePayload))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 when avatar seed changes")
    void should_Return200_when_SeedChanges() throws Exception {
        User currentUser = createAndSaveUser("juan@email.com");
        currentUser.setProfilePicture("Sad");
        userRepository.save(currentUser);

        String avatarUpdatePayload = "{\"seed\": \"Moon\"}";

        mockMvc.perform(patch("/users/myProfile/juan@email.com/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(avatarUpdatePayload))
            .andExpect(status().isOk());

        User updated = userRepository.findById("juan@email.com").get();
        assertThat(updated.getProfilePicture()).isEqualTo("Moon");
    }

    @Test
    @DisplayName("Should return 200 when setting watched films visibility to not visible")
    void should_Return200_when_WatchedFilmsSetToNotVisible() throws Exception {
        createAndSaveUser("juan@email.com");
        String visibilityPayload = "{\"isVisible\": false}";
        User updated = performPatchAndGetUpdatedUser("juan@email.com", "/users/myProfile/juan@email.com/visibilityWatchedFilms", visibilityPayload);
        assertFalse(updated.isShowWatchedFilms());
    }

    @Test
    @DisplayName("Should return 200 when setting watched films visibility to visible")
    void should_Return200_when_WatchedFilmsSetToVisible() throws Exception {
        User currentUser = createAndSaveUser("juan@email.com");
        currentUser.setShowWatchedFilms(false);
        userRepository.save(currentUser);
        String visibilityPayload = "{\"isVisible\": true}";
        User updated = performPatchAndGetUpdatedUser("juan@email.com", "/users/myProfile/juan@email.com/visibilityWatchedFilms", visibilityPayload);
        assertTrue(updated.isShowWatchedFilms());
    }

    @Test
    @DisplayName("Should return 200 when setting watch later films visibility to not visible")
    void should_Return200_when_WatchLaterFilmsSetToNotVisible() throws Exception {
        createAndSaveUser("juan@email.com");
        String visibilityPayload = "{\"isVisible\": false}";
        User updated = performPatchAndGetUpdatedUser("juan@email.com", "/users/myProfile/juan@email.com/visibilityFilmsToWatchLater", visibilityPayload);
        assertFalse(updated.isShowFilmsToWatchLater());
    }

    @Test
    @DisplayName("Should return 200 when setting watch later films visibility to visible")
    void should_Return200_when_WatchLaterFilmsSetToVisible() throws Exception {
        User currentUser = createAndSaveUser("juan@email.com");
        currentUser.setShowFilmsToWatchLater(false);
        userRepository.save(currentUser);
        String visibilityPayload = "{\"isVisible\": true}";
        User updated = performPatchAndGetUpdatedUser("juan@email.com", "/users/myProfile/juan@email.com/visibilityFilmsToWatchLater", visibilityPayload);
        assertTrue(updated.isShowFilmsToWatchLater());
    }

    @Test
    @DisplayName("Should return 200 when setting recommended films visibility to not visible")
    void should_Return200_when_RecommendedFilmsSetToNotVisible() throws Exception {
        createAndSaveUser("juan@email.com");
        String visibilityPayload = "{\"isVisible\": false}";
        User updated = performPatchAndGetUpdatedUser("juan@email.com", "/users/myProfile/juan@email.com/visibilityRecommendedFilms", visibilityPayload);
        assertFalse(updated.isShowRecommendedFilms());
    }

    @Test
    @DisplayName("Should return 200 when setting recommended films visibility to visible")
    void should_Return200_when_RecommendedFilmsSetToVisible() throws Exception {
        User currentUser = createAndSaveUser("juan@email.com");
        currentUser.setShowRecommendedFilms(false);
        userRepository.save(currentUser);
        String visibilityPayload = "{\"isVisible\": true}";
        User updated = performPatchAndGetUpdatedUser("juan@email.com", "/users/myProfile/juan@email.com/visibilityRecommendedFilms", visibilityPayload);
        assertTrue(updated.isShowRecommendedFilms());
    }
}