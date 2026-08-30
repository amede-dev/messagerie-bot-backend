package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequestDto(
        @NotBlank String contenu,
        String type, // TEXTE / IMAGE / DOCUMENT ...
        Long messageParentId
) {
}
