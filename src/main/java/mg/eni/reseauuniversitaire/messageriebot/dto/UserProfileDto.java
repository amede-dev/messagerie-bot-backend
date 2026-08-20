package mg.eni.reseauuniversitaire.messageriebot.dto;

public record UserProfileDto(
        Long id,
        String nom,
        String prenom,
        String email,
        String parcours,
        String niveau,
        String photoUrl,
        long amis,
        long groupes
) {}
