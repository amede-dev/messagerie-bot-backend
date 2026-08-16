package mg.eni.reseauuniversitaire.messageriebot.dto;

public record AuthResponseDto(
        String token,
        Long userId,
        String nom,
        String prenom,
        String email,
        String role
) {
}
