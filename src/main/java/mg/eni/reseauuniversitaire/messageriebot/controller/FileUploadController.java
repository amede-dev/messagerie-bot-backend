package mg.eni.reseauuniversitaire.messageriebot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final Path uploadDirectory;
    private final UserRepository userRepository;
    private final String supabaseUrl;
    private final String supabaseServiceRoleKey;
    private final String supabaseStorageBucket;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public FileUploadController(
            @Value("${app.upload-dir:uploads}") String uploadDir,
            UserRepository userRepository,
            @Value("${SUPABASE_URL:}") String supabaseUrl,
            @Value("${SUPABASE_SERVICE_ROLE_KEY:${SUPABASE_SERVICE_ROLE:${SUPABASE_KEY:}}}") String supabaseServiceRoleKey,
            @Value("${SUPABASE_STORAGE_BUCKET:${SUPABASE_BUCKET:message-files}}") String supabaseStorageBucket
    ) {
        this.userRepository = userRepository;
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.trim();
        this.supabaseServiceRoleKey = supabaseServiceRoleKey == null
                ? ""
                : supabaseServiceRoleKey.trim();
        this.supabaseStorageBucket = supabaseStorageBucket == null
                ? "message-files"
                : supabaseStorageBucket.trim();
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

        if (stockageSupabaseActif()) {
            log.info("Stockage des pièces jointes activé: bucket Supabase '{}'", supabaseStorageBucket);
        } else {
            log.warn(
                    "Supabase Storage est inactif: les pièces jointes seront écrites dans '{}'. "
                            + "Sur Render, configurez SUPABASE_URL et SUPABASE_SERVICE_ROLE_KEY.",
                    uploadDirectory
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

        byte[] contenu = fichier.getBytes();
        if (stockageSupabaseActif()) {
            envoyerVersSupabase(
                    nomFichier,
                    contenu,
                    fichier.getContentType()
            );
        } else {
            stockerLocalement(nomFichier, contenu);
        }

        log.info("Pièce jointe enregistrée: nom='{}', stockage='{}'", nomFichier,
                stockageSupabaseActif() ? "supabase/" + supabaseStorageBucket : uploadDirectory);

        String url =
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/files/download/")
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

    @GetMapping("/download/{nom:.+}")
    public ResponseEntity<byte[]> telecharger(@PathVariable String nom)
            throws IOException {
        if (!Paths.get(nom).getFileName().toString().equals(nom)) {
            return ResponseEntity.badRequest().build();
        }

        byte[] contenu;
        String type;

        if (stockageSupabaseActif()) {
            HttpResponse<byte[]> response;
            try {
                response = httpClient.send(
                        HttpRequest.newBuilder(uriObjetSupabase(nom))
                                .header("Authorization", "Bearer " + supabaseServiceRoleKey)
                                .header("apikey", supabaseServiceRoleKey)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Téléchargement Supabase interrompu", exception);
            }
            if (response.statusCode() != 200) {
                return ResponseEntity.status(response.statusCode()).build();
            }
            contenu = response.body();
            type = Files.probeContentType(Paths.get(nom));
        } else {
            Path fichier = uploadDirectory.resolve(nom).normalize();
            if (!fichier.startsWith(uploadDirectory) || !Files.isRegularFile(fichier)) {
                return ResponseEntity.notFound().build();
            }
            contenu = Files.readAllBytes(fichier);
            type = Files.probeContentType(fichier);
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (type != null) {
            try {
                mediaType = MediaType.parseMediaType(type);
            } catch (IllegalArgumentException ignored) {
                // Type inconnu : conserver application/octet-stream.
            }
        }

        return ResponseEntity.ok().contentType(mediaType).body(contenu);
    }

    private boolean stockageSupabaseActif() {
        return !supabaseUrl.isBlank()
                && !supabaseServiceRoleKey.isBlank()
                && !supabaseStorageBucket.isBlank()
                && supabaseUrl.startsWith("http");
    }

    private URI uriObjetSupabase(String nom) {
        return URI.create(
                supabaseUrl.replaceAll("/$", "")
                        + "/storage/v1/object/"
                        + supabaseStorageBucket
                        + "/"
                        + nom
        );
    }

    private void envoyerVersSupabase(
            String nom,
            byte[] contenu,
            String contentType
    ) throws IOException {
        try {
            HttpRequest.Builder requete = HttpRequest.newBuilder(uriObjetSupabase(nom))
                    .header("Authorization", "Bearer " + supabaseServiceRoleKey)
                    .header("apikey", supabaseServiceRoleKey)
                    .header(
                            "Content-Type",
                            contentType == null
                                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                                    : contentType
                    )
                    .header("x-upsert", "true")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(contenu));

            HttpResponse<String> response = httpClient.send(
                    requete.build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException(
                        "Supabase Storage a refusé le fichier (HTTP "
                                + response.statusCode() + ")"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload Supabase interrompu", exception);
        }
    }

    private void stockerLocalement(String nom, byte[] contenu) throws IOException {
        Path destination = uploadDirectory.resolve(nom).normalize();
        if (!destination.startsWith(uploadDirectory)) {
            throw new IOException("Chemin de fichier invalide.");
        }
        Files.write(destination, contenu, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @PostMapping(
            value = "/profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadPhotoProfil(
            @RequestParam("file") MultipartFile fichier,
            @AuthenticationPrincipal User utilisateur
    ) throws IOException {
        if (fichier == null || fichier.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("erreur", "Le fichier est vide.")
            );
        }

        utilisateur.setPhotoData(fichier.getBytes());
        utilisateur.setPhotoContentType(
                fichier.getContentType() == null
                        ? "application/octet-stream"
                        : fichier.getContentType()
        );
        utilisateur.setPhotoUrl(
                "/api/users/" + utilisateur.getId() + "/photo"
        );
        userRepository.save(utilisateur);

        return ResponseEntity.ok(
                Map.of(
                        "url", utilisateur.getPhotoUrl(),
                        "nom", fichier.getOriginalFilename() == null
                                ? "photo-profil"
                                : fichier.getOriginalFilename(),
                        "type", utilisateur.getPhotoContentType()
                )
        );
    }

    private Map<String, String> stockerFichier(MultipartFile fichier)
            throws IOException {
        String nomOriginal = fichier.getOriginalFilename();
        String extension = "";

        if (nomOriginal != null) {
            int position = nomOriginal.lastIndexOf('.');
            if (position >= 0) {
                extension = nomOriginal.substring(position).toLowerCase();
            }
        }

        String nomFichier = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(nomFichier).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IOException("Chemin de fichier invalide.");
        }

        Files.copy(
                fichier.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        String url = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(nomFichier)
                .toUriString();

        return Map.of(
                "url", url,
                "nom", nomOriginal == null ? nomFichier : nomOriginal,
                "type", fichier.getContentType() == null
                        ? "application/octet-stream"
                        : fichier.getContentType()
        );
    }
}
