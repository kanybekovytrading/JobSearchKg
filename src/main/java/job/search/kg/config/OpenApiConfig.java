package job.search.kg.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
        @Bean
        public OpenAPI openAPI() {
                Server server = new Server();
                server.setUrl("https://jobsearchkg-production.up.railway.app");
                server.setDescription("API Server");
                return new OpenAPI().addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"))
                        .servers(List.of(server))
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
}
}
