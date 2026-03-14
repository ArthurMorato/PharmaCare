package com.pharmacare.infrastructure.adapter.input.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

/**
 * Resposta paginada padrão.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada")
public class PagedResponse<T> extends RepresentationModel<PagedResponse<T>> {
    
    @Schema(description = "Lista de itens da página atual")
    private List<T> content;
    
    @Schema(description = "Número da página atual (0-based)", example = "0")
    private int pageNumber;
    
    @Schema(description = "Tamanho da página", example = "20")
    private int pageSize;
    
    @Schema(description = "Total de páginas", example = "5")
    private int totalPages;
    
    @Schema(description = "Total de elementos", example = "95")
    private long totalElements;
    
    @JsonProperty("first")
    @Schema(description = "Indica se é a primeira página", example = "true")
    private boolean firstPage;
    
    @JsonProperty("last")
    @Schema(description = "Indica se é a última página", example = "false")
    private boolean lastPage;
    
    @Schema(description = "Número de elementos na página atual", example = "20")
    private int numberOfElements;
    
    @Schema(description = "Indica se a página está vazia", example = "false")
    private boolean empty;
    
    /**
     * Cria uma PagedResponse a partir de uma Page do Spring.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .firstPage(page.isFirst())
            .lastPage(page.isLast())
            .numberOfElements(page.getNumberOfElements())
            .empty(page.isEmpty())
            .build();
    }
}