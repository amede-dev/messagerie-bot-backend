package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record BotMessageRequestDto(@NotBlank String texte) {
}
