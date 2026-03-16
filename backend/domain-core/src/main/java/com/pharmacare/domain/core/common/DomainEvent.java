package com.pharmacare.domain.core.common;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface base para todos os eventos de domínio.
 * Segue o padrão Domain Events do DDD.
 */
public interface DomainEvent {
    
    /**
     * ID único do evento.
     */
    UUID getEventId();
    
    /**
     * ID da entidade relacionada.
     */
    String getAggregateId();
    
    /**
     * Tipo da entidade (Product, Inventory, etc).
     */
    String getAggregateType();
    
    /**
     * Tipo do evento.
     */
    String getEventType();
    
    /**
     * Data e hora do evento.
     */
    LocalDateTime getOccurredOn();
    
    /**
     * Versão do evento para controle de concorrência.
     */
    int getVersion();
    
    /**
     * Dados específicos do evento.
     */
    Object getEventData();
}