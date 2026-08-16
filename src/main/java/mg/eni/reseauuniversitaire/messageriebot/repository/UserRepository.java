package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
