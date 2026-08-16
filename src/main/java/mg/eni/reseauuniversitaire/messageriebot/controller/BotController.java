package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.dto.BotMessageRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.BotResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.service.BotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;

    @PostMapping("/message")
    public BotResponseDto envoyerMessage(@Valid @RequestBody BotMessageRequestDto requete) {
        return botService.repondre(requete.texte());
    }
}
