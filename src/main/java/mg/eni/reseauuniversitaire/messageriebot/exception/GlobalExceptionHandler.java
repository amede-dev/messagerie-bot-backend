package mg.eni.reseauuniversitaire.messageriebot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> gererIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erreur", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> gererValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                erreurs.put(err.getField(), err.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(erreurs);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> gererErreurGenerique(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erreur", "Une erreur interne est survenue"));
    }
}
