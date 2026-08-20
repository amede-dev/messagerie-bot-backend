package mg.eni.reseauuniversitaire.messageriebot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Synchronise la contrainte créée lors d'une ancienne version du schéma.
 * Hibernate ddl-auto=update ne modifie pas toujours les CHECK constraints.
 */
@Component
@RequiredArgsConstructor
public class MessageTypeConstraintInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void mettreAJourContrainteTypesMessage() {
        jdbcTemplate.execute(
                "ALTER TABLE message "
                        + "DROP CONSTRAINT IF EXISTS message_type_check"
        );

        jdbcTemplate.execute(
                "ALTER TABLE message ADD CONSTRAINT message_type_check "
                        + "CHECK (type IN "
                        + "('TEXTE','IMAGE','DOCUMENT','AUDIO','VIDEO','SYSTEME'))"
        );
    }
}
