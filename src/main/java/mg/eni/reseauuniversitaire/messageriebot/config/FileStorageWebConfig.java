package mg.eni.reseauuniversitaire.messageriebot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileStorageWebConfig
        implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        String location =
                "file:"
                + uploadDirectory
                + "/";

        registry
                .addResourceHandler(
                        "/uploads/**"
                )
                .addResourceLocations(
                        location
                );
    }
}