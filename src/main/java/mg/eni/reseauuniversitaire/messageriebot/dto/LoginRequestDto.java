package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank String email, @NotBlank String motDePasse) {
}
