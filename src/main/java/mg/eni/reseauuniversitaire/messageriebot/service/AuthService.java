package mg.eni.reseauuniversitaire.messageriebot.service;

import mg.eni.reseauuniversitaire.messageriebot.dto.AuthResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.LoginRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.RegisterRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import mg.eni.reseauuniversitaire.messageriebot.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDto inscrire(RegisterRequestDto requete) {
        if (userRepository.findByEmail(requete.email()).isPresent()) {
            throw new IllegalArgumentException("Un compte existe deja avec cet email");
        }

        User user = new User();
        user.setNom(requete.nom());
        user.setPrenom(requete.prenom());
        user.setEmail(requete.email());
        user.setMotDePasse(passwordEncoder.encode(requete.motDePasse()));
        user.setRole(verifierRoleInscription(requete.role()));
        user = userRepository.save(user);

        return construireReponse(user);
    }

    public AuthResponseDto connecter(LoginRequestDto requete) {
        User user = userRepository.findByEmail(requete.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(requete.motDePasse(), user.getMotDePasse())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        return construireReponse(user);
    }

    private AuthResponseDto construireReponse(User user) {
        String token = jwtService.genererToken(user.getEmail(), user.getId());
        return new AuthResponseDto(
                token, user.getId(), user.getNom(), user.getPrenom(), user.getEmail(), user.getRole().name()
        );
    }

    private User.Role verifierRoleInscription(String role) {
    String roleNormalise = role.trim().toUpperCase(java.util.Locale.ROOT);

    if (!roleNormalise.equals("ETUDIANT")
            && !roleNormalise.equals("ENSEIGNANT")) {
        throw new IllegalArgumentException("Rôle non autorisé.");
    }

    return User.Role.valueOf(roleNormalise);
}
}
