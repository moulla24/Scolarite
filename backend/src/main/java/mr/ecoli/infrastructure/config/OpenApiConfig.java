package mr.ecoli.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI ecoliOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecoli API — Scolarité primaire")
                        .description(
                                "API REST pour la gestion de scolarité. "
                                        + "Connectez-vous via POST /auth/login, copiez le token, puis cliquez sur « Authorize ».")
                        .version("1.0.0")
                        .contact(new Contact().name("Ecoli").email("contact@ecoli.mr")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token obtenu via POST /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
