package software.ulpgc.netlikes.controller;

import software.ulpgc.netlikes.dto.UserRequestDTO;
import software.ulpgc.netlikes.dto.UserResponseDTO;
import software.ulpgc.netlikes.dto.ChangePasswordDTO;
import software.ulpgc.netlikes.dto.LoginRequestDTO;
import software.ulpgc.netlikes.dto.UserProfileDTO;
import software.ulpgc.netlikes.dto.PrivacyRequestDTO;
import software.ulpgc.netlikes.dto.RegisterRequestDTO;
import software.ulpgc.netlikes.dto.ValidAnswerRequestDTO;
import software.ulpgc.netlikes.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
        @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "20") int size,
        @RequestParam String mail
    ) {
        return userService.getAllUsers(page, size, mail);
    }

    @GetMapping("/{email}")
    public UserResponseDTO getUserById(@NonNull @PathVariable String email) {
        return userService.getUserById(email);
    }

    @PutMapping("/{email}")
    public UserResponseDTO updateUser(
            @NonNull @PathVariable String email,
            @Valid @RequestBody UserRequestDTO dto) {

        return userService.updateUser(email, dto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader("X-User-Id") String myId) {
        userService.deleteUser(myId);
        
        return ResponseEntity.ok().build(); 
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<?> existsEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.existsEmail(email));
    }

    @GetMapping("/securityQuestion/{email}")
    public ResponseEntity<?> getSecurityQuestion(@NonNull @PathVariable String email) {
        try {
            return ResponseEntity.ok(userService.getSecurityQuestion(email));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/isValidAnswer")
    public ResponseEntity<?> isValidAnswer(@NonNull @RequestBody ValidAnswerRequestDTO request) {
        try {
            return ResponseEntity.ok(userService.isValidAnswer(Objects.requireNonNull(request.getEmail()), request.getAnswer()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO request) {
        try {
            userService.changePassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/myProfile/{email}")
    public UserProfileDTO myProfile(@NonNull @PathVariable String email){
        return userService.myProfile(email);
    }
    
    @GetMapping("/profile/{userName}")
    public UserProfileDTO userProfile(@PathVariable String userName, @RequestParam String requesterEmail){
        return userService.userProfile(userName, requesterEmail);
    }

    @PatchMapping("/myProfile/{email}/privacy")
    public ResponseEntity<?> changePrivacy(@NonNull @PathVariable String email, @RequestBody PrivacyRequestDTO request) {
        userService.changePrivacy(email, request.getIsPrivate());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchFilm(@RequestParam String query) {
        return this.userService.searchBy(query);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Map<String, Boolean>> verifyPassword(
            @RequestHeader("X-User-Id") String myId,
            @RequestBody Map<String, String> requestBody) {
        
        String password = requestBody.get("password");
        boolean isValid = userService.verifyPassword(myId, password);
        
        if (isValid) {
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }
    }
}
