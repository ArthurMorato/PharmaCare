package com.pharmacare.infrastructure.adapter.input.rest.assembler;

import com.pharmacare.application.service.product.dto.ProductResult;
import com.pharmacare.infrastructure.adapter.input.rest.controller.v1.ProductControllerV1;
import com.pharmacare.infrastructure.adapter.input.rest.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler para adicionar links HATEOAS às respostas de produto.
 */
@Component
@RequiredArgsConstructor
public class ProductAssembler 
    extends RepresentationModelAssemblerSupport<ProductResult, ProductResponse> {
    
    public ProductAssembler() {
        super(ProductControllerV1.class, ProductResponse.class);
    }
    
    @Override
    public ProductResponse toModel(ProductResult productResult) {
        ProductResponse response = mapToResponse(productResult);
        
        // Adiciona links HATEOAS
        response.add(
            linkTo(methodOn(ProductControllerV1.class)
                .getProductById(productResult.id()))
                .withSelfRel()
        );
        
        response.add(
            linkTo(methodOn(ProductControllerV1.class)
                .updateProduct(productResult.id(), null))
                .withRel("update")
        );
        
        response.add(
            linkTo(methodOn(ProductControllerV1.class)
                .deleteProduct(productResult.id()))
                .withRel("delete")
        );
        
        response.add(
            linkTo(methodOn(ProductControllerV1.class)
                .searchProducts(null, null))
                .withRel("products")
        );
        
        return response;
    }
    
    @Override
    public CollectionModel<ProductResponse> toCollectionModel(
        Iterable<? extends ProductResult> productResults
    ) {
        CollectionModel<ProductResponse> collectionModel = super.toCollectionModel(productResults);
        
        collectionModel.add(
            linkTo(methodOn(ProductControllerV1.class)
                .createProduct(null))
                .withRel("create")
        );
        
        collectionModel.add(
            linkTo(methodOn(ProductControllerV1.class)
                .searchProducts(null, null))
                .withRel("search")
        );
        
        return collectionModel;
    }
    
    private ProductResponse mapToResponse(ProductResult productResult) {
        return ProductResponse.builder()
            .id(productResult.id())
            .name(productResult.name())
            .barcode(productResult.barcode())
            .description(productResult.description())
            .category(productResult.category())
            .price(productResult.price())
            .currency(productResult.currency())
            .minimumStock(productResult.minimumStock())
            .currentStock(productResult.currentStock())
            .manufacturer(productResult.manufacturer())
            .activePrinciple(productResult.activePrinciple())
            .concentration(productResult.concentration())
            .unit(productResult.unit())
            .status(productResult.status())
            .prescriptionRequired(productResult.requiresPrescription())
            .imageUrl(productResult.imageUrl())
            .taxable(productResult.taxable())
            .icmsTaxRate(productResult.icmsTaxRate())
            .createdAt(productResult.createdAt())
            .updatedAt(productResult.updatedAt())
            .metadata(productResult.metadata())
            .version(productResult.version())
            .build();
    }
}