package com.pharmacare.infrastructure.adapter.input.rest.config;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuração customizada do Swagger/OpenAPI.
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Adiciona schemas comuns
            openApi.getComponents()
                .addSchemas("ErrorResponse", createErrorResponseSchema())
                .addSchemas("PagedResponse", createPagedResponseSchema());
            
            // Configuração global de respostas
            openApi.getPaths().values().forEach(pathItem -> 
                pathItem.readOperations().forEach(operation -> {
                    // Adiciona respostas padrão para todos os endpoints
                    operation.getResponses().addApiResponse("500", 
                        new io.swagger.v3.oas.models.responses.ApiResponse()
                            .description("Erro interno do servidor")
                            .content(new io.swagger.v3.oas.models.media.Content()
                                .addMediaType("application/json",
                                    new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                                )
                            )
                    );
                })
            );
        };
    }
    
    private Schema<?> createErrorResponseSchema() {
        return new Schema<Map<String, Object>>()
            .type("object")
            .addProperty("timestamp", new StringSchema().example("2024-01-15T14:30:00"))
            .addProperty("status", new Schema<>().type("integer").example(400))
            .addProperty("error", new StringSchema().example("Bad Request"))
            .addProperty("message", new StringSchema().example("Erro de validação"))
            .addProperty("path", new StringSchema().example("/api/v1/products"))
            .addProperty("errorCode", new StringSchema().example("VALIDATION_ERROR"))
            .description("Resposta padronizada de erro");
    }
    
    private Schema<?> createPagedResponseSchema() {
        return new Schema<Map<String, Object>>()
            .type("object")
            .addProperty("content", new Schema<>().type("array"))
            .addProperty("pageNumber", new Schema<>().type("integer").example(0))
            .addProperty("pageSize", new Schema<>().type("integer").example(20))
            .addProperty("totalPages", new Schema<>().type("integer").example(5))
            .addProperty("totalElements", new Schema<>().type("integer").example(95))
            .addProperty("first", new Schema<>().type("boolean").example(true))
            .addProperty("last", new Schema<>().type("boolean").example(false))
            .description("Resposta paginada");
    }
}