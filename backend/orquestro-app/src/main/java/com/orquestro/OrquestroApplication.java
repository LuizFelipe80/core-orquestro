package com.orquestro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Classe principal de inicialização do Orquestro.
 * Configurada para escanear entidades e repositórios em todos os módulos.
 */
@SpringBootApplication(scanBasePackages = "com.orquestro")
@EntityScan(basePackages = "com.orquestro")
@EnableJpaRepositories(basePackages = "com.orquestro")
public class OrquestroApplication {

    /**
     * Ponto de entrada principal da aplicação.
     *
     * @param args Argumentos de linha de comando.
     */
    public static void main(String[] args) {
        SpringApplication.run(OrquestroApplication.class, args);
    }
}