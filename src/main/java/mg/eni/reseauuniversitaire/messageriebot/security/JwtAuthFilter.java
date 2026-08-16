package mg.eni.reseauuniversitaire.messageriebot.security;

import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/// Intercepte chaque requete HTTP, lit le header "Authorization: Bearer ...",
/// valide le token JWT et place l'utilisateur authentifie dans le contexte
/// Spring Security pour le reste de la requete.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        if (jwtService.estValide(token)) {
            final String email = jwtService.extraireEmail(token);
            Optional<User> utilisateur = userRepository.findByEmail(email);

            if (utilisateur.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        utilisateur.get(), null, List.of() // roles a affiner selon User.Role si besoin
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
