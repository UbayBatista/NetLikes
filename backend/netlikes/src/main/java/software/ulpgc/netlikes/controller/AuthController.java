package software.ulpgc.netlikes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        
        Cookie springCookie = new Cookie("JSESSIONID", null);
        springCookie.setMaxAge(0);
        springCookie.setPath("/");
        response.addCookie(springCookie);

        Cookie discourseCookie = new Cookie("_t", null);
        discourseCookie.setMaxAge(0);
        discourseCookie.setPath("/");
        discourseCookie.setDomain("duckdns.org"); 
        
        response.addCookie(discourseCookie);

        return ResponseEntity.ok().body(Map.of("message", "Sesión y cookies eliminadas"));
    }
}