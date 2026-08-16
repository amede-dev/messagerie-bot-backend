package mg.eni.reseauuniversitaire.messageriebot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey cle() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String genererToken(String email, Long userId) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(cle())
                .compact();
    }

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public Long extraireUserId(String token) {
        return extraireClaim(token, claims -> claims.get("userId", Long.class));
    }

    public boolean estValide(String token) {
        try {
            return !estExpire(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(cle()).build().parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }
}
