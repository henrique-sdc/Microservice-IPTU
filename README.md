# Microsserviço de IPTU - Springfield

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-green.svg) <!-- Verifique a versão exata -->
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![OpenFeign](https://img.shields.io/badge/Spring%20Cloud-OpenFeign-brightgreen.svg)
![Maven](https://img.shields.io/badge/Maven-Gestor-red.svg)

## 📌 Visão Geral

Este é o microsserviço dedicado ao gerenciamento do IPTU da cidade de Springfield. Ele fornece endpoints para:

-   Gerar o carnê de IPTU anual (12 parcelas ou cota única).
-   Consultar a situação de débitos e pagamentos de um carnê.
-   Registrar o pagamento de parcelas.

Este serviço depende do `Projeto-Springfield` (executado na porta 8080) para validar a existência dos cidadãos antes de gerar o IPTU, utilizando **Spring Cloud OpenFeign** para a comunicação.

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.4.x**
- **Spring Data JPA**
- **Spring Web**
- **Spring Cloud OpenFeign**
- **MySQL 8.0**
- **Lombok**
- **Maven**
- **JUnit 5 / Mockito** (para testes com Feign)

## 📋 Pré-requisitos

Antes de executar este serviço, instale/tenha:

- **[JDK 17](https://www.oracle.com/java/technologies/downloads/)**
- **[Maven](https://maven.apache.org/download.cgi)**
- **[MySQL Server e Workbench](https://www.mysql.com/products/workbench/)**
- **Uma IDE** (IntelliJ IDEA, Eclipse, VS Code)
- **O serviço `Projeto-Springfield` DEVE estar em execução** (normalmente na porta 8080).

## 📂 Estrutura do Projeto

```
iptu-service/
├── src/
│ ├── main/
│ │ ├── java/com/springfield/iptu/
│ │ │ ├── client/ # Clientes Feign (CidadaoClient)
│ │ │ ├── controller/ # Endpoints REST (IptuController)
│ │ │ ├── dto/ # Data Transfer Objects
│ │ │ ├── model/ # Entidades JPA (IptuAnual, ParcelaIPTU)
│ │ │ ├── repository/ # Repositórios JPA
│ │ │ ├── service/ # Regras de negócio (IptuService)
│ │ │ └── IptuServiceApplication.java
│ │ ├── resources/
│ │ │ └── application.properties # Configurações específicas do IPTU
│ └── test/ # Testes de Integração com Feign
│ ├── java/com/springfield/iptu/
│ │ ├── client/ # Cliente Feign para os testes
│ │ └── service/ # Classe de teste (IptuControllerFeignTest)
└── pom.xml # Configuração do Maven para este serviço
└── README.md # Este arquivo
```

## ⚙️ Configuração

1.  **Clone o repositório principal** (Se ainda não o fez)
    **E clone este repositório:**
    ```bash
    git clone https://github.com/henrique-sdc/Projeto-Springfield.git
    git clone https://github.com/henrique-sdc/Microservice-IPTU.git
    cd iptu-service
    ```

3.  **Banco de Dados:**
    *   Este serviço utiliza o mesmo banco de dados `db_springfield` criado para o `Projeto-Springfield`.
    *   Certifique-se de que as seguintes tabelas **adicionais** existam (além das tabelas `CAD_CIDADAO` e `CAD_USUARIO_CIDADAO` usadas pelo outro serviço):
        ```sql
        CREATE TABLE IF NOT EXISTS IPTU_ANUAL (
            id INT PRIMARY KEY AUTO_INCREMENT,
            cidadao_id INT NOT NULL,
            ano INT NOT NULL,
            valor_total DECIMAL(10, 2) NOT NULL DEFAULT 12000.00,
            tipo_pagamento VARCHAR(10) NOT NULL,
            data_geracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE KEY uk_cidadao_ano (cidadao_id, ano)
        );
        
        CREATE TABLE IF NOT EXISTS PARCELA_IPTU (
            id INT PRIMARY KEY AUTO_INCREMENT,
            iptu_anual_id INT NOT NULL,
            numero_parcela INT NOT NULL,
            valor DECIMAL(10, 2) NOT NULL,
            data_vencimento DATE NOT NULL,
            pago BOOLEAN NOT NULL DEFAULT FALSE,
            data_pagamento TIMESTAMP NULL,
            FOREIGN KEY (iptu_anual_id) REFERENCES IPTU_ANUAL(id) ON DELETE CASCADE,
            UNIQUE KEY uk_iptu_anual_parcela (iptu_anual_id, numero_parcela)
        );
        
        CREATE TABLE IF NOT EXISTS SOLICITACAO_HISTORICO (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            demanda_id BINARY(16) NOT NULL,
            cidadao_id INT NOT NULL,
            descricao_demanda VARCHAR(500) NOT NULL,
            estado VARCHAR(50) NOT NULL,
            evento VARCHAR(50) NULL,
            data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            INDEX idx_demanda_id (demanda_id),
            INDEX idx_cidadao_id (cidadao_id),
            CONSTRAINT fk_solicitacao_cidadao FOREIGN KEY (cidadao_id) REFERENCES CAD_CIDADAO(ID) ON DELETE CASCADE
        );
        ```
    *   O `spring.jpa.hibernate.ddl-auto=update` pode tentar criar/atualizar essas tabelas.

4.  **Configure `application.properties`:**
    *   Ajuste as configurações no arquivo `src/main/resources/application.properties`:
    ```properties
    spring.application.name=iptu-service
    server.port=8081 # Porta para este serviço

    spring.datasource.url=jdbc:mysql://localhost:3306/db_springfield?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    spring.datasource.username=root # SEU_USUARIO_MYSQL
    spring.datasource.password=root # SUA_SENHA_MYSQL
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    logging.level.org.hibernate.SQL=DEBUG
    logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

    # URL onde o Projeto-Springfield está rodando
    cidadao.service.url=http://localhost:8080
    ```
    *   **Importante:** Confirme que as credenciais do banco e a `cidadao.service.url` estão corretas.

5.  **Importe o projeto `iptu-service` na sua IDE.**

## ▶️ Executando o Serviço

**Ordem Obrigatória:**

1.  **PRIMEIRO:** Inicie o serviço `springfield-api` (normalmente na porta 8080).
2.  **DEPOIS:** Inicie este serviço (`iptu-service`).
    *   Via IDE: Execute a classe `IptuServiceApplication.java`.
    *   Via Maven (na pasta `iptu-service`):
        ```bash
        mvn spring-boot:run
        ```
    *   Este serviço estará disponível na porta `8081`.

## 🚀 Testando a API (Postman - Exemplos)

Use o Postman ou similar para interagir com a API na porta `8081`. Uma coleção completa com mais cenários pode estar disponível no repositório.

-   **Gerar IPTU:**
    ```http
    POST http://localhost:8081/iptu/gerar
    Content-Type: application/json

    {
      "cidadaoId": 10001,
      "ano": 2025,
      "tipoPagamento": "PARCELADO" // ou "UNICO"
    }
    ```

-   **Consultar Situação:**
    ```http
    GET http://localhost:8081/iptu/10001/2025/situacao
    ```

-   **Pagar Parcela:**
    ```http
    PUT http://localhost:8081/iptu/10001/2025/parcela/1/pagar
    ```

## 🧪 Testes Automatizados (OpenFeign)

Este projeto inclui testes de integração (`IptuControllerFeignTest.java`) que validam os endpoints da API de IPTU, simulando (mockando) a chamada externa para o serviço de cidadão.

-   **Executar via IDE:** Encontre a classe de teste e use a opção "Run Tests".
-   **Executar via Maven:** Na pasta `iptu-service`, execute `mvn test`.
