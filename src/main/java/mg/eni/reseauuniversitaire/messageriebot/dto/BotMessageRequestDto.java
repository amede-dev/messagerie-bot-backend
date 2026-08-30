package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.NotBlank;

public record BotMessageRequestDto(@NotBlank String texte) {
}
