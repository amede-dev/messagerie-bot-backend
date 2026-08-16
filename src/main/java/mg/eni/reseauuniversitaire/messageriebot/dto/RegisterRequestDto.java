package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caracteres")
        String motDePasse
) {
}
