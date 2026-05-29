package software.ulpgc.netlikes.integration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.config.TestConfig;
import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.HuggingFaceService;
import software.ulpgc.netlikes.service.UserService;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Import(TestConfig.class)
public class UserServiceIT {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Genre actionGenre;
    private Genre comedyGenre;
    private Genre dramaGenre;

    @MockBean 
    private HuggingFaceService huggingFaceService;

    @BeforeEach
    void setUp() {
        when(huggingFaceService.generateVector(anyString())).thenReturn("[0.0, 0.0]");

        actionGenre = saveGenre(1, "Acción");
        comedyGenre = saveGenre(2, "Comedia");
        dramaGenre = saveGenre(3, "Drama");

        User existingUser = createUser("registrado@email.com", "UsuarioPillado", "123456", "¿Mascota?", "Toby");
        userRepository.save(existingUser);
    }

    @Test
    @DisplayName("Should register successfully and save the user in the database when the data is valid")
    void should_RegisterSuccessfullyAndSaveUserInDatabase_when_registrationDataIsValid() {
        RegisterRequestDTO request = createRegisterRequest("UsuarioNuevo", "nuevo@email.com", "Password123", List.of(actionGenre, comedyGenre, dramaGenre));

        userService.register(request);

        assertTrue(userRepository.existsByEmail("nuevo@email.com"), "El usuario debería estar guardado en la BD");
        assertTrue(userRepository.existsByName("UsuarioNuevo"), "El nombre de usuario debería estar ocupado ahora");
    }

    @Test
    @DisplayName("Should throw an exception when the email already exists")
    void should_ThrowException_when_emailAlreadyExists() {
        RegisterRequestDTO request = createRegisterRequest("NombreLibre", "registrado@email.com", "Password123", List.of(actionGenre, comedyGenre, dramaGenre));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });
        
        assertEquals("El email ya está registrado", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw an exception when the username already exists")
    void should_ThrowException_when_usernameAlreadyExists() {
        RegisterRequestDTO request = createRegisterRequest("UsuarioPillado", "email_libre@email.com", "Password123", List.of(actionGenre, comedyGenre, dramaGenre));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });
        
        assertEquals("El nombre de usuario ya está en uso", exception.getMessage());
    }

    @Test
    @DisplayName("Should login successfully and return the user when credentials are correct")
    void should_LoginSuccessfullyAndReturnUser_when_credentialsAreCorrect() {
        LoginRequestDTO request = createLoginRequest("registrado@email.com", "123456");

        UserResponseDTO response = userService.login(request);

        assertNotNull(response);
        assertEquals("registrado@email.com", response.getEmail(), "El email devuelto debe coincidir");
        assertEquals("UsuarioPillado", response.getUserName(), "El nombre devuelto debe coincidir");
    }

    @Test
    @DisplayName("Should throw an exception when the password is incorrect")
    void should_ThrowException_when_passwordIsIncorrect() {
        LoginRequestDTO request = createLoginRequest("registrado@email.com", "claveMala");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales incorrectas", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw an exception when the email does not exist")
    void should_ThrowException_when_emailDoesNotExist() {
        LoginRequestDTO request = createLoginRequest("fantasma@email.com", "123456");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales incorrectas", exception.getMessage());
    }

    @Test
    @DisplayName("Should change the password successfully and allow login with the new password")
    void should_ChangePasswordSuccessfullyAndAllowLoginWithNewPassword_when_passwordIsUpdated() {
        String email = "registrado@email.com";
        String newPassword = "nuevaClave987";

        userService.changePassword(email, newPassword);

        assertThrows(RuntimeException.class, () -> userService.login(createLoginRequest(email, "123456")));

        UserResponseDTO response = userService.login(createLoginRequest(email, newPassword));
        assertNotNull(response);
        assertEquals(email, response.getEmail());
    }

    private Genre saveGenre(int id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genreRepository.save(genre);
    }

    private User createUser(String email, String name, String password, String securityQuestion, String answer) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setBirthdate(new Date(System.currentTimeMillis()));
        user.setSecurityQuestion(securityQuestion);
        user.setAnswer(answer);
        user.setVector("[0.0, 0.0]");
        return user;
    }

    private RegisterRequestDTO createRegisterRequest(String name, String email, String password, List<Genre> genres) {
        return new RegisterRequestDTO(
            name,
            email,
            new Date(System.currentTimeMillis()),
            password,
            "¿Color?",
            "Rojo",
            genres
        );
    }

    private LoginRequestDTO createLoginRequest(String email, String password) {
        return new LoginRequestDTO(email, password);
    }
}

