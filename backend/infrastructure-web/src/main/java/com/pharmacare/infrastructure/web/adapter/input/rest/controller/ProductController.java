package com.pharmacare.infrastructure.adapter.input.rest.controller;

import com.pharmacare.infrastructure.adapter.input.rest.controller.v1.ProductControllerV1;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller principal que redireciona para a versão atual da API.
 * Ponto único de entrada para compatibilidade.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController extends ProductControllerV1 {
    
    // Herda todos os endpoints da v1
    // Quando houver v2, esta classe pode fazer versionamento por header
    // ou delegar para a versão apropriada
    
    public ProductController(
        com.pharmacare.application.service.product.ProductService productService,
        com.pharmacare.infrastructure.adapter.input.rest.assembler.ProductAssembler productAssembler
    ) {
        super(productService, productAssembler);
    }
}