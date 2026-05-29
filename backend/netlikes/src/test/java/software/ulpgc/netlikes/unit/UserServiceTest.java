package software.ulpgc.netlikes.unit;

import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.service.DiscourseService;
import software.ulpgc.netlikes.service.HuggingFaceService;
import software.ulpgc.netlikes.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HuggingFaceService huggingFaceService;

    @Mock
    private DiscourseService discourseService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should register successfully and return user DTO when email is new")
    void should_RegisterSuccessfullyAndReturnUserDto_when_emailIsNew() {
        Genre actionGenre = createGenre(21, "Acción");
        Genre dramaGenre = createGenre(23, "Drama");
        Genre horrorGenre = createGenre(15, "Terror");

        List<Genre> selectedGenres = List.of(actionGenre, dramaGenre, horrorGenre);
        
        List<Integer> genreIds = selectedGenres.stream().map(Genre::getId).toList();

        RegisterRequestDTO request = new RegisterRequestDTO(
            "Juan", 
            "juan@email.com",
            Date.valueOf("1900-05-21"),
            "SuperMan23",
            "Nombre de tu primera mascota",
            "Toby",
            selectedGenres
        );

        when(userRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(passwordEncoder.encode("SuperMan23")).thenReturn("hashedPassword");
        
        when(genreRepository.findAllById(genreIds)).thenReturn(selectedGenres);
        
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO userResponse = userService.register(request);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getEmail()).isEqualTo("juan@email.com");
        assertThat(userResponse.getUserName()).isEqualTo("Juan");

        org.mockito.Mockito.verify(genreRepository).findAllById(genreIds);
        org.mockito.Mockito.verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw an exception when the email already exists")
    void should_ThrowException_when_emailAlreadyExists() {
        Genre actionGenre = createGenre(21, "Acción");
        Genre dramaGenre = createGenre(23, "Drama");

        RegisterRequestDTO request = new RegisterRequestDTO
        ("Juan", 
        "juan@email.com",
        Date.valueOf("2002-11-15"),
        "SuperMan23",
        "Nombre de tu primera mascota",
        "Toby",
        List.of(actionGenre, dramaGenre)
        );

        when(userRepository.existsByEmail("juan@email.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Should throw an exception when the name already exists")
    void should_ThrowException_when_nameAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO(
        "Juan", "nuevo_email@email.com", Date.valueOf("2002-11-15"), "SuperMan23", "Mascota", "Toby", List.of()
        );

        when(userRepository.existsByEmail("nuevo_email@email.com")).thenReturn(false);
        when(userRepository.existsByName("Juan")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Should login successfully and return user DTO when credentials are correct")
    void should_LoginSuccessfullyAndReturnUserDto_when_credentialsAreCorrect() {
        LoginRequestDTO request = new LoginRequestDTO("juan@email.com", "SuperMan23");

        User user = createUser("juan@email.com", "Juan", "hashedPassword", null, null);

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SuperMan23", "hashedPassword")).thenReturn(true);

        UserResponseDTO userResponse = userService.login(request);

        assertThat(userResponse.getEmail()).isEqualTo("juan@email.com");
    }

    @Test
    @DisplayName("Should throw an exception when the password is wrong")
    void should_ThrowException_when_passwordIsWrong() {
        LoginRequestDTO request = new LoginRequestDTO("juan@email.com", "wrongPassword");

        User user = createUser("juan@email.com", null, "hashedPassword", null, null);

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Should throw an exception when the user does not exist")
    void should_ThrowException_when_userDoesNotExist() {
        LoginRequestDTO request = new LoginRequestDTO("joss@email.com", "SuperMan23");

        when(userRepository.findById("joss@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Should return true when the email exists")
    void should_ReturnTrue_when_emailExists() {
        when(userRepository.existsByEmail("juan@email.com")).thenReturn(true);

        boolean result = userService.existsEmail("juan@email.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when the email does not exist")
    void should_ReturnFalse_when_emailDoesNotExist() {
        when(userRepository.existsByEmail("noexiste@email.com")).thenReturn(false);

        boolean result = userService.existsEmail("noexiste@email.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when the name exists")
    void should_ReturnTrue_when_nameExists() {
        when(userRepository.existsByName("Juan")).thenReturn(true);
        assertThat(userService.existsName("Juan")).isTrue();
    }

    @Test
    @DisplayName("Should return false when the name does not exist")
    void should_ReturnFalse_when_nameDoesNotExist() {
        when(userRepository.existsByName("noexiste")).thenReturn(false);
        assertThat(userService.existsName("noexiste")).isFalse();
    }

    @Test
    @DisplayName("Should return the security question when the user exists")
    void should_ReturnSecurityQuestion_when_userExists() {
        User user = createUser("juan@email.com", null, null, "¿Nombre de tu mascota?", null);

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));

        String result = userService.getSecurityQuestion("juan@email.com");

        assertThat(result).isEqualTo("¿Nombre de tu mascota?");
    }

    @Test
    @DisplayName("Should throw an exception when the user does not exist")
    void should_ThrowException_when_userDoesNotExistAgain() {
        when(userRepository.findById("noexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getSecurityQuestion("noexiste@email.com"));
    }

    @Test
    @DisplayName("Should return true when the answer is correct")
    void should_ReturnTrue_when_answerIsCorrect() {
        User user = createUser("juan@email.com", null, null, null, "Firulais");

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));

        boolean result = userService.isValidAnswer("juan@email.com", "Firulais");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when the answer is wrong")
    void should_ReturnFalse_when_answerIsWrong() {
        User user = createUser("juan@email.com", null, null, null, "Firulais");

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));

        boolean result = userService.isValidAnswer("juan@email.com", "RespuestaErronea");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should throw an exception when the credentials are invalid")
    void should_ThrowException_when_credentialsAreInvalid() {
        when(userRepository.findById("noexiste@email.com")).thenReturn(Optional.empty());

        LoginRequestDTO request = new LoginRequestDTO("noexiste@email.com", "1234");

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("Should encode and save the new password when the user exists")
    void should_EncodeAndSaveNewPassword_when_userExists() {
        String email = "juan@email.com";
        String newPassword = "newSecretPassword";
        User user = createUser(email, null, "oldHashedPassword", null, null);

        when(userRepository.findById(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");

        userService.changePassword(email, newPassword);

        assertThat(user.getPassword()).isEqualTo("newHashedPassword");
        org.mockito.Mockito.verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw an exception when the user does not exist")
    void should_ThrowException_when_userDoesNotExistForPasswordChange() {
        when(userRepository.findById("noexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            userService.changePassword("noexiste@email.com", "anyPassword")
        );
    }

    @Test
    @DisplayName("Should delete the user when the user exists")
    void should_DeleteUser_when_userExists() {

        User mockUser = new User();
        mockUser.setEmail("juan@email.com");

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(mockUser));

        when(discourseService.getDiscourseUserId(any())).thenReturn(123);

        userService.deleteUser("juan@email.com");

        verify(userRepository).deleteById("juan@email.com");
    }

    @Test
    @DisplayName("Should throw an exception when the user does not exist")
    void should_ThrowException_when_userDoesNotExistForDeletion() {
        when(userRepository.findById("noexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.deleteUser("noexiste@email.com"));
    }

    @Test
    @DisplayName("Should update the biography when the user exists")
    void should_UpdateBio_when_userExists() {
        User user = createUser("juan@email.com", null, null, null, null);
        user.setBio("Bio antigua");

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateBio("juan@email.com", "Nueva bio");

        assertThat(user.getBio()).isEqualTo("Nueva bio");
        org.mockito.Mockito.verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw an exception when the user does not exist")
    void should_ThrowException_when_userDoesNotExistForBioUpdate() {
        when(userRepository.findById("noexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateBio("noexiste@email.com", "Bio"));
    }

    @Test
    @DisplayName("Should update the avatar when the user exists")
    void should_UpdateAvatar_when_userExists() {
        User user = createUser("juan@email.com", null, null, null, null);

        when(userRepository.findById("juan@email.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateAvatar("juan@email.com", "Moon");

        assertThat(user.getProfilePicture()).isEqualTo("Moon");
        org.mockito.Mockito.verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw an exception when the user does not exist")
    void should_ThrowException_when_userDoesNotExistForAvatarUpdate() {
        when(userRepository.findById("noexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateAvatar("noexiste@email.com", "Sad"));
    }
    private Genre createGenre(int id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }

    private User createUser(String email, String name, String password, String securityQuestion, String answer) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setSecurityQuestion(securityQuestion);
        user.setAnswer(answer);
        user.setVector("");
        return user;
    }}