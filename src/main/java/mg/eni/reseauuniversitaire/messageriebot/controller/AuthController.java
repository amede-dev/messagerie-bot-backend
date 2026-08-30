package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.dto.AuthResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.LoginRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.RegisterRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Ces deux endpoints sont les SEULS accessibles sans token JWT
// (voir SecurityConfig : "/api/auth/**" est en permitAll()).
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> inscrire(@Valid @RequestBody RegisterRequestDto requete) {
        return ResponseEntity.ok(authService.inscrire(requete));
    }

    @PostMapping("/login")
    public ResponseEntity<?> connecter(@Valid @RequestBody LoginRequestDto requete) {
        try {
            return ResponseEntity.ok(authService.connecter(requete));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("erreur", e.getMessage()));
        }
    }
}
