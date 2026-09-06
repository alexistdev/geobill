# GeoLicense — Spring Boot Client Setup

Compatible with **Spring Boot 3.x** and **Java 21+**.

---

## 1. Install the JAR

The starter is distributed as a JAR. Install it into your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=geolicense-client-starter-1.0.1.jar \
  -DgroupId=com.alexistdev \
  -DartifactId=geolicense-client-starter \
  -Dversion=1.0.1 \
  -Dpackaging=jar
```

Then add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alexistdev</groupId>
    <artifactId>geolicense-client-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

---

## 2. Configure Properties

Add to your `application.properties`:

```properties
geolicense.server-url=http://localhost:8082
geolicense.license-key=XXXX-XXXX-XXXX-XXXX
geolicense.product-sku=BILL
```

Optional overrides (defaults shown):

```properties
geolicense.verify-interval-ms=3600000
geolicense.grace-period-minutes=30
geolicense.exclude-paths=/actuator/**,/health/**
```

Or in `application.yml`:

```yaml
geolicense:
  server-url: http://localhost:8082
  license-key: XXXX-XXXX-XXXX-XXXX
  product-sku: BILL
  verify-interval-ms: 3600000
  grace-period-minutes: 30
  exclude-paths:
    - /actuator/**
    - /health/**
```

---

## 3. No Extra Code Required

The starter uses Spring Boot autoconfiguration. As long as `geolicense.license-key` is set, all beans are registered automatically — no `@Import` or `@ComponentScan` changes needed.

---

## 4. Verify It Works

Start your application. On startup you should see:

```
License activated successfully for key: XXXX-XXXX****
```

If activation fails, the application context will **refuse to start** with an `IllegalStateException`.

---

## How It Works

| Step | What happens |
|---|---|
| App starts | `ApplicationRunner` calls `POST /api/v1/licenses/activate` with `{licenseKey, machineId, productSku, osInfo}` |
| Activation succeeds | Token stored in `LicenseHolder` in memory; `valid = true` |
| Activation fails | Application context startup is aborted (hard fail) |
| Every request | `LicenseValidationFilter` checks `LicenseHolder.isValid()` |
| Periodically | `LicenseVerificationScheduler` calls `POST /api/v1/licenses/verify` on the configured interval |
| Server unreachable during verify | Grace period allows traffic for N minutes after last successful check |
| License invalid / expired | Returns HTTP 503 JSON: `{"status": false, "messages": ["License invalid or expired"]}` |

---

## Machine ID

The machine ID is derived from a SHA-256 hash of the MAC address + hostname. If that fails (e.g., no network interface), a UUID is generated and persisted to `~/.geolicense-machine-id`.

---

## Exclude Paths from License Check

Paths matching Ant-style patterns bypass the filter entirely. Defaults: `/actuator/**`, `/health/**`.

To override:

```properties
geolicense.exclude-paths=/actuator/**,/health/**,/webhook/**
```

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `geolicense.server-url` | — | **Required.** Base URL of the GeoLicense server |
| `geolicense.license-key` | — | **Required.** License key; autoconfiguration is disabled if absent |
| `geolicense.product-sku` | — | **Required.** SKU of the product this app is licensed for (e.g. `BILL`); activation fails if absent |
| `geolicense.verify-interval-ms` | `3600000` (1 h) | How often to re-verify the license in milliseconds |
| `geolicense.grace-period-minutes` | `30` | Minutes traffic is allowed after the last successful verification |
| `geolicense.exclude-paths` | `/actuator/**`, `/health/**` | Ant-pattern paths exempt from the license filter |
