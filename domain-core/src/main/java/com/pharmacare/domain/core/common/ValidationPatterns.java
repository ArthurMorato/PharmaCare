package com.pharmacare.domain.core.common;

import java.util.regex.Pattern;

/**
 * Padrões de validação reutilizáveis para Value Objects.
 */
public final class ValidationPatterns {
    
    // Padrão para nomes de produtos: letras, números, espaços e alguns caracteres especiais
    public static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile(
        "^[\\p{L}\\p{N}\\s\\-\\.,;:'\"()\\[\\]!?]{3,100}$",
        Pattern.UNICODE_CHARACTER_CLASS
    );
    
    // Padrão para EAN-13: exatamente 13 dígitos
    public static final Pattern EAN13_PATTERN = Pattern.compile("^\\d{13}$");
    
    // Padrão para descrições: qualquer caractere, mas com limites de comprimento
    public static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
        "^.{0,500}$",
        Pattern.DOTALL
    );
    
    // Padrão para marcas: semelhante ao nome do produto
    public static final Pattern BRAND_PATTERN = Pattern.compile(
        "^[\\p{L}\\p{N}\\s\\-\\.,&]{1,50}$",
        Pattern.UNICODE_CHARACTER_CLASS
    );
    
    // Valida dígito verificador EAN-13
    public static boolean isValidEAN13CheckDigit(String barcode) {
        if (barcode == null || barcode.length() != 13) {
            return false;
        }
        
        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(barcode.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            
            int checkDigit = (10 - (sum % 10)) % 10;
            int actualCheckDigit = Character.getNumericValue(barcode.charAt(12));
            
            return checkDigit == actualCheckDigit;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Valida se um UUID é válido
    public static boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        return Pattern.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", uuid.toLowerCase());
    }
    
    private ValidationPatterns() {
        // Classe utilitária - não instanciável
    }
}