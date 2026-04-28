package software.ulpgc.netlikes.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.UserRequestDTO;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GenreRepository genreRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UserResponseDTO getUserById(String email) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toDTO(user);
    }

    public UserResponseDTO createUser(UserRequestDTO dto) {

        User user = new User();
        applyDtoToEntity(dto, user);

        userRepository.save(user);
        return toDTO(user);
    }

    public UserResponseDTO updateUser(String email, UserRequestDTO dto) {

        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        applyDtoToEntity(dto, user);

        userRepository.save(user);
        return toDTO(user);
    }

    public void deleteUser(String email) {
        userRepository.deleteById(email);
    }

    public UserResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findById(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        return toDTO(user);
    }

    public UserResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User newUser = new User();
        newUser.setName(request.getUserName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setSecurityQuestion(request.getSecurityQuestion());
        newUser.setAnswer(request.getAnswer());
        newUser.setBirthdate(request.getBirthdate());
        if (request.getFavoriteGenres() != null) {
            List<Integer> ids = request.getFavoriteGenres().stream()
                                    .map(g -> (int) g.getId())
                                    .toList();
            List<Genre> genres = genreRepository.findAllById(ids);
            newUser.setFavoriteGenres(genres);
        }

        User saved = userRepository.save(newUser);
        return toDTO(saved);
    }

    public boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public String getSecurityQuestion(String email) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getSecurityQuestion();
    }

    public boolean isValidAnswer(String email, String answer) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getAnswer().equals(answer);
    }

    private void applyDtoToEntity(UserRequestDTO dto, User user) {
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setSecurityQuestion(dto.getSecurityQuestion());
        user.setAnswer(dto.getAnswer());
        user.setName(dto.getName());
        user.setBirthdate(dto.getBirthdate());
        user.setAccountPrivacity(dto.isAccountPrivacity());
        user.setShowWatchedFilms(dto.isShowWatchedFilms());
        user.setShowFilmsToWatchLater(dto.isShowFilmsToWatchLater());
        user.setShowRecommendedFilms(dto.isShowRecommendedFilms());
        user.setProfilePicture(dto.getProfilePicture());
        user.setBio(dto.getBio());

        List<Genre> genres = genreRepository.findAllById(dto.getFavoriteGenresIds());
        user.setFavoriteGenres(genres);
    }

    private UserResponseDTO toDTO(User user) {

        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setProfilePicture(user.getProfilePicture());

        return dto;
    }
}
