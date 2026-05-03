package software.ulpgc.netlikes.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import software.ulpgc.netlikes.dto.FilmResponseDTO;
import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.UserProfileDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.UserRequestDTO;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.model.Follow;
import software.ulpgc.netlikes.model.Genre;
import software.ulpgc.netlikes.model.User;
import software.ulpgc.netlikes.repository.FollowRepository;
import software.ulpgc.netlikes.repository.GenreRepository;
import software.ulpgc.netlikes.repository.UserRepository;
import software.ulpgc.netlikes.model.Mark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final FollowService followService;
    private final MarkService markService;

    public UserService(UserRepository userRepository, GenreRepository genreRepository, FollowRepository followRepository, PasswordEncoder passwordEncoder, FollowService followService, MarkService markService) {
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.followService = followService; 
        this.markService = markService;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ResponseEntity<List<UserResponseDTO>> getAllUsers(int page, int size, String mail) {
        PageRequest paginacion = PageRequest.of(page, size);

        List<UserResponseDTO> catalogo = userRepository.findAll(paginacion).getContent()
        .stream()
        .filter(u -> !u.getEmail().equals(mail))
        .map(this::toDTO)
        .toList();
        
        return ResponseEntity.ok(catalogo);
    }

    public ResponseEntity<List<UserResponseDTO>> searchBy(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        Pageable topTen = PageRequest.of(0, 10);
        List<UserResponseDTO> results = userRepository.findByNameContainingIgnoreCase(query, topTen).stream().map(this::toDTO).toList();
        
        return ResponseEntity.ok(results);
    }

    public UserResponseDTO getUserById(@NonNull String email) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toDTO(user);
    }

    public boolean isPrivate(@NonNull String email){
        return userRepository.findById(email)
        .orElseThrow(() -> new RuntimeException("User not found"))
        .isAccountPrivacity();
    }

    public UserResponseDTO createUser(UserRequestDTO dto) {

        User user = new User();
        applyDtoToEntity(dto, user);

        userRepository.save(user);
        return toDTO(user);
    }

    public UserResponseDTO updateUser(@NonNull String email, UserRequestDTO dto) {

        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getName().equals(dto.getName()) && userRepository.existsByName(dto.getName())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        applyDtoToEntity(dto, user);

        userRepository.save(user);
        return toDTO(user);
    }

    public void deleteUser(@NonNull String email) {
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
        
        if (userRepository.existsByName(request.getUserName())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
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

    public String getSecurityQuestion(@NonNull String email) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getSecurityQuestion();
    }

    public boolean existsName(String name) {
        return userRepository.existsByName(name);
    }

    public boolean isValidAnswer(@NonNull String email, String answer) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return user.getAnswer().equals(answer);
    }

    
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserProfileDTO myProfile(@NonNull String email){
        User user = userRepository.findById(email)
            .orElseThrow(()-> new RuntimeException("Usuario no encontrado"));
        
        List<FilmResponseDTO> watchedFilms = markService.getFilmsByMarkType(email, Mark.Type.SEEN);
        List<FilmResponseDTO> watchLaterFilms = markService.getFilmsByMarkType(email, Mark.Type.WATCHLATER);

        return new UserProfileDTO(
            user.getEmail(),
            user.getName(),
            user.getBio(),
            user.isAccountPrivacity(),
            followService.countFollowersOf(user.getEmail()),
            followService.countFollowsOf(user.getEmail()),
            watchedFilms, 
            watchLaterFilms
        );
    }

    public UserProfileDTO userProfile(String userName, String requesterEmail) {
        User target = userRepository.findByName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isOwnProfile = target.getEmail().equals(requesterEmail);
        boolean isFollowing = (followService.checkStatus(requesterEmail, target.getEmail()).equals("ACCEPTED"));

        boolean canSeeContent = !target.isAccountPrivacity() || isOwnProfile || isFollowing;

        List<FilmResponseDTO> watched = canSeeContent ? 
            markService.getFilmsByMarkType(target.getEmail(), Mark.Type.SEEN) : new ArrayList<>();
        
        List<FilmResponseDTO> later = canSeeContent ? 
            markService.getFilmsByMarkType(target.getEmail(), Mark.Type.WATCHLATER) : new ArrayList<>();

        return new UserProfileDTO(
            target.getEmail(),
            target.getName(),
            target.getBio(),
            target.isAccountPrivacity(),
            followService.countFollowersOf(target.getEmail()),
            followService.countFollowsOf(target.getEmail()),
            canSeeContent ? watched : null,
            canSeeContent ? later : null
        );
    }

    public void changePrivacy(@NonNull String email, Boolean isPrivate) {
        User user = userRepository.findById(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setAccountPrivacity(isPrivate);
        userRepository.save(user);

        if (!isPrivate) {
        followRepository.findByFollowedId(email).stream()
            .filter(follow -> follow.getState() == Follow.State.PENDING)
            .forEach(follow -> {
                follow.setState(Follow.State.ACCEPTED);
                followRepository.save(follow);
            });
        }
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

