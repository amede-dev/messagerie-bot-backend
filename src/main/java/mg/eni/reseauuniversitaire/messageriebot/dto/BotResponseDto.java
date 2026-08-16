package mg.eni.reseauuniversitaire.messageriebot.dto;

import java.util.List;

/// Correspond exactement a BotResponseModel.fromJson cote Flutter :
/// texte / suggestions / escaladeHumaine.
public record BotResponseDto(
        String texte,
        List<String> suggestions,
        boolean escaladeHumaine
) {
}
