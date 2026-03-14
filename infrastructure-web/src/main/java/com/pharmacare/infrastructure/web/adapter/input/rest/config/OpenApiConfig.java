package com.pharmacare.infrastructure.adapter.input.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI 3 (Swagger) para documentação da API.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "PharmaCare ERP API",
        version = "1.0.0",
        description = """
            API do Sistema ERP PharmaCare para gerenciamento de farmácias.
            
            ### Recursos disponíveis:
            - **Gestão de Produtos**: Cadastro, consulta, atualização e exclusão de produtos
            - **Gestão de Estoque**: Controle de entradas, saídas e inventário
            - **Gestão de Vendas**: Processamento de vendas e emissão de notas fiscais
            - **Gestão de Clientes**: Cadastro e histórico de compras
            - **Relatórios**: Geração de relatórios gerenciais
            
            ### Autenticação:
            Use o endpoint `/api/auth/login` para obter um token JWT.
            Inclua o token no header `Authorization: Bearer <token>`.
            
            ### Contato:
            - Suporte técnico: suporte@pharmacare.com.br
            - Site: https://www.pharmacare.com.br
            """,
        contact = @Contact(
            name = "Suporte PharmaCare",
            email = "suporte@pharmacare.com.br",
            url = "https://www.pharmacare.com.br"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        ),
        termsOfService = "https://www.pharmacare.com.br/terms"
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Servidor de Desenvolvimento"
        ),
        @Server(
            url = "https://api.staging.pharmacare.com.br",
            description = "Servidor de Staging"
        ),
        @Server(
            url = "https://api.pharmacare.com.br",
            description = "Servidor de Produção"
        )
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    in = SecuritySchemeIn.HEADER,
    description = """
        Autenticação JWT via Bearer Token.
        
        Para obter um token:
        1. Faça login em `/api/auth/login` com suas credenciais
        2. Use o token retornado no header `Authorization: Bearer <token>`
        """
)
public class OpenApiConfig {
    
    // Configuração adicional pode ser feita via beans
    // se necessário customizar mais
}