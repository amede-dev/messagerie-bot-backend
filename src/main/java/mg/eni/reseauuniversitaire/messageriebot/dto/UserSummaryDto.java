package mg.eni.reseauuniversitaire.messageriebot.dto;

public record UserSummaryDto(
        Long id,
        String nom,
        String prenom,
        String email,
        String photoUrl,
        boolean enLigne
) {
}
