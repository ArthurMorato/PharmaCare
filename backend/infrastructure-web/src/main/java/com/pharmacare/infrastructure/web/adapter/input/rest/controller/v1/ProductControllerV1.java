package com.pharmacare.infrastructure.adapter.input.rest.controller.v1;

import com.pharmacare.application.service.product.ProductService;
import com.pharmacare.application.service.product.dto.CreateProductCommand;
import com.pharmacare.application.service.product.dto.UpdateProductCommand;
import com.pharmacare.infrastructure.adapter.input.rest.assembler.ProductAssembler;
import com.pharmacare.infrastructure.adapter.input.rest.dto.request.CreateProductRequest;
import com.pharmacare.infrastructure.adapter.input.rest.dto.request.SearchProductRequest;
import com.pharmacare.infrastructure.adapter.input.rest.dto.request.UpdateProductRequest;
import com.pharmacare.infrastructure.adapter.input.rest.dto.response.PagedResponse;
import com.pharmacare.infrastructure.adapter.input.rest.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST v1 para gerenciamento de produtos.
 * Versionamento: /api/v1
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Product Management",
    description = "API para gerenciamento de produtos no sistema PharmaCare"
)
@SecurityRequirement(name = "bearerAuth")
public class ProductControllerV1 {
    
    private final ProductService productService;
    private final ProductAssembler productAssembler;
    
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PHARMACIST', 'MANAGER', 'ADMIN')")
    @Operation(
        summary = "Criar novo produto",
        description = "Cria um novo produto no sistema. Requer permissão de farmacêutico ou superior."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Produto criado com sucesso",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Não autorizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito (ex: código de barras já existe)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request
    ) {
        log.info("POST /api/v1/products - Criando novo produto: {}", request.getName());
        
        CreateProductCommand command = CreateProductCommand.builder()
            .name(request.getName())
            .barcode(request.getBarcode())
            .description(request.getDescription())
            .category(request.getCategory())
            .price(request.getPrice())
            .minimumStock(request.getMinimumStock())
            .manufacturer(request.getManufacturer())
            .activePrinciple(request.getActivePrinciple())
            .concentration(request.getConcentration())
            .unit(request.getUnit())
            .requiresPrescription(request.isRequiresPrescription())
            .imageUrl(request.getImageUrl())
            .taxable(request.isTaxable())
            .icmsTaxRate(request.getIcmsTaxRate())
            .build();
        
        var result = productService.createProduct(command);
        var response = productAssembler.toModel(result);
        
        log.info("Produto criado com ID: {}", result.id());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
    
    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PHARMACIST', 'CASHIER', 'MANAGER', 'ADMIN')")
    @Operation(
        summary = "Buscar produto por ID",
        description = "Recupera um produto específico pelo seu ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductResponse> getProductById(
        @Parameter(description = "ID do produto", example = "prod-1234567890")
        @PathVariable String id
    ) {
        log.info("GET /api/v1/products/{} - Buscando produto por ID", id);
        
        var result = productService.getProductById(id);
        var response = productAssembler.toModel(result);
        
        log.debug("Produto encontrado: {}", result.name());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('PHARMACIST', 'CASHIER', 'MANAGER', 'ADMIN')")
    @Operation(
        summary = "Buscar produtos com filtros",
        description = "Recupera produtos com filtros, paginação e ordenação."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Produtos encontrados",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de busca inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
        @ParameterObject @Valid SearchProductRequest searchRequest,
        @Parameter(hidden = true) Pageable pageable
    ) {
        log.info("GET /api/v1/products - Buscando produtos com filtros: {}", searchRequest);
        
        // Cria Pageable customizado a partir dos parâmetros de busca
        Sort.Direction direction = Sort.Direction.fromString(searchRequest.getSortDirection());
        Sort sort = Sort.by(direction, searchRequest.getSortBy());
        Pageable customPageable = PageRequest.of(
            searchRequest.getPage(), 
            searchRequest.getSize(), 
            sort
        );
        
        var pageResult = productService.searchProducts(
            searchRequest.getName(),
            searchRequest.getBarcode(),
            searchRequest.getCategory(),
            searchRequest.getStatus(),
            searchRequest.getRequiresPrescription(),
            searchRequest.getManufacturer(),
            searchRequest.getMinPrice(),
            searchRequest.getMaxPrice(),
            searchRequest.getIds(),
            customPageable
        );
        
        var responses = pageResult.getContent().stream()
            .map(productAssembler::toModel)
            .toList();
        
        var pagedResponse = PagedResponse.<ProductResponse>builder()
            .content(responses)
            .pageNumber(pageResult.getNumber())
            .pageSize(pageResult.getSize())
            .totalPages(pageResult.getTotalPages())
            .totalElements(pageResult.getTotalElements())
            .firstPage(pageResult.isFirst())
            .lastPage(pageResult.isLast())
            .numberOfElements(pageResult.getNumberOfElements())
            .empty(pageResult.isEmpty())
            .build();
        
        // Adiciona links HATEOAS para paginação
        if (pageResult.hasNext()) {
            pagedResponse.add(
                linkTo(methodOn(ProductControllerV1.class)
                    .searchProducts(searchRequest, pageable))
                    .withRel("next")
            );
        }
        
        if (pageResult.hasPrevious()) {
            pagedResponse.add(
                linkTo(methodOn(ProductControllerV1.class)
                    .searchProducts(searchRequest, pageable))
                    .withRel("previous")
            );
        }
        
        log.debug("Encontrados {} produtos", pageResult.getTotalElements());
        return ResponseEntity.ok(pagedResponse);
    }
    
    @PutMapping(
        value = "/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PHARMACIST', 'MANAGER', 'ADMIN')")
    @Operation(
        summary = "Atualizar produto",
        description = "Atualiza um produto existente. Requer permissão de farmacêutico ou superior."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Produto atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflito de versão (otimista)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductResponse> updateProduct(
        @Parameter(description = "ID do produto", example = "prod-1234567890")
        @PathVariable String id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("PUT /api/v1/products/{} - Atualizando produto", id);
        
        UpdateProductCommand command = UpdateProductCommand.builder()
            .id(id)
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .minimumStock(request.getMinimumStock())
            .manufacturer(request.getManufacturer())
            .status(request.getStatus())
            .imageUrl(request.getImageUrl())
            .requiresPrescription(request.isRequiresPrescription())
            .build();
        
        var result = productService.updateProduct(command);
        var response = productAssembler.toModel(result);
        
        log.info("Produto atualizado: {}", id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
        summary = "Excluir produto (lógico)",
        description = "Realiza exclusão lógica do produto. Requer permissão de gerente ou superior."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Produto excluído com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Não autorizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> deleteProduct(
        @Parameter(description = "ID do produto", example = "prod-1234567890")
        @PathVariable String id
    ) {
        log.info("DELETE /api/v1/products/{} - Excluindo produto", id);
        
        productService.deleteProduct(id);
        
        log.info("Produto excluído (lógico): {}", id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping(
        value = "/barcode/{barcode}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PHARMACIST', 'CASHIER', 'MANAGER', 'ADMIN')")
    @Operation(
        summary = "Buscar produto por código de barras",
        description = "Recupera um produto específico pelo seu código de barras."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Produto encontrado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Código de barras inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductResponse> getProductByBarcode(
        @Parameter(description = "Código de barras EAN-13", example = "7891234567890")
        @PathVariable String barcode
    ) {
        log.info("GET /api/v1/products/barcode/{} - Buscando produto por código de barras", barcode);
        
        var result = productService.getProductByBarcode(barcode);
        var response = productAssembler.toModel(result);
        
        log.debug("Produto encontrado pelo barcode: {}", barcode);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping(
        value = "/category/{category}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('PHARMACIST', 'CASHIER', 'MANAGER', 'ADMIN')")
    @Operation(
        summary = "Listar produtos por categoria",
        description = "Recupera produtos de uma categoria específica."
    )
    public ResponseEntity<CollectionModel<ProductResponse>> getProductsByCategory(
        @PathVariable String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/v1/products/category/{} - Listando produtos por categoria", category);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var pageResult = productService.getProductsByCategory(category, pageable);
        
        var responses = pageResult.getContent().stream()
            .map(productAssembler::toModel)
            .toList();
        
        var collectionModel = CollectionModel.of(responses);
        
        // Adiciona links para paginação
        if (pageResult.hasNext()) {
            collectionModel.add(
                linkTo(methodOn(ProductControllerV1.class)
                    .getProductsByCategory(category, page + 1, size))
                    .withRel("next")
            );
        }
        
        if (page > 0) {
            collectionModel.add(
                linkTo(methodOn(ProductControllerV1.class)
                    .getProductsByCategory(category, page - 1, size))
                    .withRel("previous")
            );
        }
        
        log.debug("Encontrados {} produtos na categoria {}", pageResult.getTotalElements(), category);
        return ResponseEntity.ok(collectionModel);
    }
}