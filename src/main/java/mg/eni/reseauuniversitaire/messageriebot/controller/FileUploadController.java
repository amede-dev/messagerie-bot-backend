package mg.eni.reseauuniversitaire.messageriebot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import mg.eni.reseauuniversitaire.messageriebot.entity.User;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final Path uploadDirectory;

    public FileUploadController(
            @Value("${app.upload-dir:uploads}") String uploadDir
    ) {
        this.uploadDirectory =
                Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize();

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de créer le dossier uploads",
                    e
            );
        }
    }

    // =========================================================================
    // UPLOAD
    // =========================================================================

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile fichier,
            @AuthenticationPrincipal User utilisateur
    ) throws IOException {

        if (fichier == null || fichier.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "erreur",
                            "Le fichier est vide."
                    )
            );
        }

        String nomOriginal =
                fichier.getOriginalFilename();

        String extension = "";

        if (nomOriginal != null) {
            int position =
                    nomOriginal.lastIndexOf('.');

            if (position >= 0) {
                extension =
                        nomOriginal.substring(position)
                                .toLowerCase();
            }
        }

        String nomFichier =
                UUID.randomUUID()
                        .toString()
                + extension;

        Path destination =
                uploadDirectory.resolve(
                        nomFichier
                ).normalize();

        if (!destination.startsWith(
                uploadDirectory
        )) {
            throw new IOException(
                    "Chemin de fichier invalide."
            );
        }

        Files.copy(
                fichier.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        String url =
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/uploads/")
                        .path(nomFichier)
                        .toUriString();

        return ResponseEntity.ok(
                Map.of(
                        "url", url,
                        "nom", nomOriginal == null
                                ? nomFichier
                                : nomOriginal,
                        "type", fichier.getContentType() == null
                                ? "application/octet-stream"
                                : fichier.getContentType()
                )
        );
    }
}