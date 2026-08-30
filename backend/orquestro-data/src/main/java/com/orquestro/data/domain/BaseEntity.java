package com.orquestro.data.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe base para todas as entidades do sistema Orquestro.
 * Implementa o uso de UUID como identificador único para aumentar a segurança,
 * evitando a exposição de IDs sequenciais em URLs e APIs.
 * Inclui campos de auditoria básica para rastreabilidade de registros.
 *
 * @author Orquestro Team
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Identificador único universal (UUID).
     * Utilizado como chave primária para garantir unicidade e segurança.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Data e hora de criação do registro.
     * Preenchido automaticamente no momento da persistência inicial.
     */
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Data e hora da última atualização do registro.
     * Atualizado automaticamente sempre que a entidade sofrer alterações.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Controle de versionamento para Lock Otimista.
     * Previne problemas de concorrência onde dois processos tentam atualizar o mesmo registro simultaneamente.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Método executado antes da persistência inicial da entidade.
     * Garante que o campo createdAt seja preenchido com o timestamp atual.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Método executado antes de qualquer atualização da entidade.
     * Atualiza o campo updatedAt com o timestamp do momento da alteração.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}