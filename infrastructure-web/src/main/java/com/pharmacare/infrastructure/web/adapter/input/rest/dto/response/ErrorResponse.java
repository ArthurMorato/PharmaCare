package com.pharmacare.infrastructure.adapter.input.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resposta padronizada para erros.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {
    
    @Schema(
        description = "Timestamp do erro",
        example = "2024-01-15T14:30:00"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Schema(
        description = "Código HTTP de status",
        example = "400"
    )
    private int status;
    
    @Schema(
        description = "Descrição do status HTTP",
        example = "BAD_REQUEST"
    )
    private String error;
    
    @Schema(
        description = "Mensagem de erro",
        example = "Código de barras já cadastrado"
    )
    private String message;
    
    @Schema(
        description = "Caminho da requisição",
        example = "/api/v1/products"
    )
    private String path;
    
    @Schema(
        description = "Código de erro interno da aplicação",
        example = "PRODUCT_BARCODE_EXISTS"
    )
    private String errorCode;
    
    @Schema(
        description = "Lista de erros de validação (quando aplicável)"
    )
    private List<ValidationError> validationErrors;
    
    @Schema(
        description = "Detalhes adicionais do erro"
    )
    private Map<String, Object> details;
    
    /**
     * Erro de validação específico.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Erro de validação de campo")
    public static class ValidationError {
        
        @Schema(
            description = "Nome do campo com erro",
            example = "barcode"
        )
        private String field;
        
        @Schema(
            description = "Valor rejeitado",
            example = "123"
        )
        private Object rejectedValue;
        
        @Schema(
            description = "Mensagem de erro",
            example = "O código de barras deve ter 13 dígitos"
        )
        private String message;
    }
    
    /**
     * Factory method para criar ErrorResponse.
     */
    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .build();
    }
    
    /**
     * Factory method com error code.
     */
    public static ErrorResponse of(HttpStatus status, String message, String path, String errorCode) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .errorCode(errorCode)
            .build();
    }
}