package software.ulpgc.netlikes.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.UserService;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
@ActiveProfiles("test")
@Transactional
public class UserBehaviorTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private Genre accion;
    private Genre comedia;
    private Genre drama;

    @BeforeEach
    void setUp() {
        accion = new Genre(); 
        accion.setId(1); 
        accion.setName("Acción");
        genreRepository.save(accion);

        comedia = new Genre(); 
        comedia.setId(2); 
        comedia.setName("Comedia");
        genreRepository.save(comedia);

        drama = new Genre(); 
        drama.setId(3); 
        drama.setName("Drama");
        genreRepository.save(drama);

        User existingUser = new User();
        existingUser.setEmail("registrado@email.com");
        existingUser.setName("UsuarioPillado");
        existingUser.setPassword(passwordEncoder.encode("123456")); 
        existingUser.setBirthdate(new Date(System.currentTimeMillis()));
        existingUser.setSecurityQuestion("¿Mascota?");
        existingUser.setAnswer("Toby");
        userRepository.save(existingUser);
    }

    @Test
    void testHU_RegistroCorrecto_SavesUserInDatabase() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "UsuarioNuevo", 
            "nuevo@email.com", 
            new Date(System.currentTimeMillis()),
            "Password123", 
            "¿Color?", 
            "Rojo", 
            List.of(accion, comedia, drama)
        );

        userService.register(request);

        assertTrue(userRepository.existsByEmail("nuevo@email.com"), "El usuario debería estar guardado en la BD");
        assertTrue(userRepository.existsByName("UsuarioNuevo"), "El nombre de usuario debería estar ocupado ahora");
    }

    @Test
    void testHU_RegistroFallido_EmailYaExiste() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "NombreLibre", 
            "registrado@email.com",
            new Date(System.currentTimeMillis()),
            "Password123", 
            "?", 
            "!", 
            List.of(accion, comedia, drama)
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });
        
        assertEquals("El email ya está registrado", exception.getMessage());
    }

    @Test
    void testHU_RegistroFallido_NombreDeUsuarioYaExiste() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "UsuarioPillado",
            "email_libre@email.com", 
            new Date(System.currentTimeMillis()),
            "Password123", 
            "?", 
            "!", 
            List.of(accion, comedia, drama)
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(request);
        });
        
        assertEquals("El nombre de usuario ya está en uso", exception.getMessage());
    }

    @Test
    void testHU_LoginCorrecto_RetornaUsuario() {
        LoginRequestDTO request = new LoginRequestDTO("registrado@email.com", "123456");

        UserResponseDTO response = userService.login(request);

        assertNotNull(response);
        assertEquals("registrado@email.com", response.getEmail(), "El email devuelto debe coincidir");
        assertEquals("UsuarioPillado", response.getUserName(), "El nombre devuelto debe coincidir");
    }

    @Test
    void testHU_LoginFallido_ContrasenaIncorrecta() {
        LoginRequestDTO request = new LoginRequestDTO("registrado@email.com", "claveMala");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales incorrectas", exception.getMessage());
    }

    @Test
    void testHU_LoginFallido_EmailNoExiste() {
        LoginRequestDTO request = new LoginRequestDTO("fantasma@email.com", "123456");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(request);
        });

        assertEquals("Credenciales incorrectas", exception.getMessage());
    }
}