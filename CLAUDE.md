# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-chain crypto payment gateway backend based on RuoYi-Vue-Plus. Java 17 + Spring Boot 3.4.6 + Undertow, Maven multi-module project.

## Modules & Ports

| Module | Type | Port | Description |
|------|------|------|-------------|
| `XPayLabs` | Spring Boot App | 8077 | User-facing payment API service |
| `XPayLabs-merchant` | Spring Boot App | 8078 | Merchant management (Sa-Token auth) |
| `XPayLabs-eth` | Spring Boot App | 8076 | EVM multi-chain scanner (ETH/BSC/Polygon/Avalanche) |
| `XPayLabs-tron` | Spring Boot App | 8075 | TRON chain scanner |
| `XPayLabs-sui` | Spring Boot App | 8074 | SUI chain scanner (calls external sui-node-service) |
| `XPayLabs-core` | Library JAR | — | Shared core (all ruoyi-common-* modules) |
| `XPayLabs-admin` | Library JAR | — | Admin dependency (wraps ruoyi-admin + yan-user) |
| `yan-crypto-payment` | Library JAR | — | Payment processing core (strategy factory pattern) |
| `yan-crypto-payment-eth` | Library JAR | — | EVM payment impl (web3j) |
| `yan-crypto-payment-tron` | Library JAR | — | TRON payment impl (gRPC) |
| `yan-user` / `yan-user-api` | Library JAR | — | User module (JustAuth social login) |
| `yan-blockchain-tron` | Library JAR | — | TRON proto definitions (system-scoped JAR) |

## Build & Run

```bash
# Build (tests skipped by default)
mvn clean install -P dev

# Production packaging
mvn clean package -P prod

# Build single module with dependencies
mvn clean package -pl XPayLabs -am -P dev

# Start a single service
mvn spring-boot:run -pl XPayLabs -P dev
```

**Running tests**: Maven Surefire defaults to `<skip>true</skip>`. Change to `<skip>false</skip>` first, then:
```bash
mvn test -pl XPayLabs -Dtest=XpayTest -P dev
mvn test -pl XPayLabs -Dtest=XpayTest#getTronPlatformFee -P dev
```

## Profiles & Config

- **dev** (active by default): dev config, p6spy SQL analysis enabled
- **prod**: production config
- Profile injected into `application.yml` via `${profiles.active}`
- DB/Redis env var overrides: `db_url`, `db_username`, `db_password`, `redis_host`, `redis_port`
- Defaults: MySQL `localhost:3306/xpaylabs`, Redis `127.0.0.1:6379` database 6

## Architecture

### Layering
- **Controller** — REST endpoints, param validation (`@Valid`), delegates to Service
- **Service** — business logic, transaction boundaries
- **Mapper** — MyBatis-Plus data access (`@TableName` + `@TableId`)
- **Domain** — POJOs (`bo`/`req`/`vo` sub-packages)

### Payment Processing Framework (yan-crypto-payment)
- Strategy factory pattern: `yan-crypto-payment` abstract layer + `yan-crypto-payment-eth`/`yan-crypto-payment-tron` implementations
- Annotation-driven: `@PaymentStrategy` marks concrete strategies
- Event-driven: `ApplicationEventPublisher` for module decoupling

### Security & Auth
- User API (8077): `X-API-TOKEN` header (API Key + HMAC signature)
- Merchant dashboard (8078): **Sa-Token** (`Authorization` header, JWT mode)
- API payload encryption: RSA key pairs, `encrypt-key` request header
- MyBatis data encryption: BASE64 algorithm
- XSS filtering: enabled by default

### Object Mapping
- Uses **mapstruct-plus** (linpeilie) for compile-time AutoMapper generation
- Generated files in `target/generated-sources/annotations/` (do not commit)
- Never use `BeanUtils.copyProperties`

### Key Dependencies
- **web3j** 4.13.0 — EVM blockchain interaction
- **gRPC** 1.57.0 — TRON blockchain interaction
- **lock4j** — Redis distributed lock (acquire-timeout 3s, expire 30s)
- **warm-flow** — workflow engine (disabled by default)
- **snail-job** — distributed job scheduling (disabled by default)

### API Docs
- SpringDoc OpenAPI enabled only on 8077: `http://localhost:8077/v3/api-docs`
- Chain scanner modules have springdoc disabled

### Logging
- Output to `./logs/`, organized by module directory
- Files: `sys-info.log`, `sys-console.log`, `sys-error.log`
- Daily rotation

## Pitfalls

1. **Tests skipped by default** — change `<skip>true</skip>` to run them
2. **yan-blockchain-tron** is commented out in parent POM but still used as system-scoped dependency
3. **Generated files not committed** — `target/generated-sources/annotations/` and `.flattened-pom.xml`
4. **Resource filtering** — only `application*`, `bootstrap*`, `banner*` files get Maven variable substitution
5. **MapStruct + Lombok** — requires `lombok-mapstruct-binding` dependency for compatibility
