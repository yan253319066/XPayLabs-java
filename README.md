# XPay Labs (xpay) Java Backend — Self-Hosted Crypto Payment Gateway Core

**XPay Labs (xpay) Java Backend** is the core runtime of the XPay Labs self-hosted crypto payment gateway. Built with Spring Boot 3.4 + MyBatis-Plus + MySQL + Redis, it handles order lifecycle management, deposit address generation, blockchain transaction verification, webhook dispatch, and merchant authentication.

## Architecture

Maven multi-module project (12 modules) organized around the RuoYi-Vue-Plus framework:

```
XPayLabs-java/
  ├── XPayLabs                  # User-facing API service (:8077)
  ├── XPayLabs-admin            # Admin management dependency module
  ├── XPayLabs-merchant         # Merchant management API (:8078)
  ├── XPayLabs-core             # Shared core (common utilities, security)
  ├── XPayLabs-eth              # EVM chain scanner (:8076)
  │                             #   ETH, BSC, Polygon, Avalanche
  ├── XPayLabs-tron             # TRON chain scanner (:8075)
  ├── XPayLabs-sui              # SUI chain scanner (:8074)
  ├── yan-user / yan-user-api   # User module (feign client + implementation)
  ├── yan-crypto-payment        # Payment processing core
  ├── yan-crypto-payment-eth    # EVM payment handlers
  └── yan-crypto-payment-tron   # TRON payment handlers
```

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Runtime** | Java 17, Spring Boot 3.4.6, Undertow |
| **Database** | MySQL 8.0, Redis 7 (lock4j distributed lock) |
| **ORM** | MyBatis-Plus (with mybatis-encryptor) |
| **Auth** | Sa-Token (merchant), API Key HMAC (user-facing) |
| **Blockchain** | web3j (EVM), TronWeb (TRON), @mysten/sui (via proxy) |
| **Mapping** | mapstruct-plus (compile-time code gen) |
| **API Docs** | SpringDoc OpenAPI (user service: :8077/v3/api-docs) |

## Modules & Ports

| Module | Port | Role |
|--------|------|------|
| `XPayLabs` | 8077 | User API — order creation, collection, payout |
| `XPayLabs-merchant` | 8078 | Merchant API — dashboard backend, Sa-Token auth |
| `XPayLabs-eth` | 8076 | EVM blockchain scanner (ETH, BSC, Polygon, Avalanche) |
| `XPayLabs-tron` | 8075 | TRON blockchain scanner |
| `XPayLabs-sui` | 8074 | SUI blockchain scanner (calls sui-node-service proxy) |

## Build

```bash
# Compile with dev profile (tests skipped by default)
mvn clean install -P dev

# Compile with prod profile
mvn clean install -P prod

# Package without tests
mvn clean package -P dev -DskipTests
```

> Tests are **skipped by default** via `<skip>true</skip>` in maven-surefire-plugin. To run tests, set `<skip>false</skip>` in `pom.xml`.

## Key Configuration

- **Maven mirror**: Huawei Cloud (`mirrors.huaweicloud.com`) — configured in `pom.xml`
- **Database**: MySQL `localhost:3306/xpaylabs` (overridable via `db_url`, `db_username`, `db_password` env vars)
- **Redis**: `127.0.0.1:6379` database 6 (overridable via `redis_host`, `redis_port` env vars)
- **Data encryption**: mybatis-encryptor (BASE64) enabled by default; API payload encryption via RSA key pairs
- **Logs**: Output to `./logs/` directory, organized by module with daily rotation

## Related Projects

- [Deployment (Docker Compose)](https://github.com/yan253319066/XPayLabs-docker)
- [Node.js SDK](https://github.com/yan253319066/XPayLabs-node-sdk)
- [Java SDK](https://github.com/yan253319066/XPayLabs-java-sdk)
- [Merchant Dashboard (Vue 3)](https://github.com/yan253319066/XPayLabs-merchant-vue)
- [SUI Node Service](https://github.com/yan253319066/XPayLabs-sui-node-service)

## License

MIT
