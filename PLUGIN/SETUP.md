# GeoLicense — Spring Boot Client Setup

Compatible with **Spring Boot 3.x** and **Java 21+**.

---

## 1. Install the JAR

The starter is distributed as a JAR. Install it into your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=geolicense-client-starter-1.0.3.jar \
  -DgroupId=com.alexistdev \
  -DartifactId=geolicense-client-starter \
  -Dversion=1.0.3 \
  -Dpackaging=jar
```

Then add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alexistdev</groupId>
    <artifactId>geolicense-client-starter</artifactId>
    <version>1.0.3</version>
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
geolicense.state-dir=${user.home}/.geolicense
geolicense.machine-id=
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
| App starts | `ApplicationRunner` loads the activation stored in `state-dir` and calls `POST /api/v1/licenses/verify` first |
| Stored activation still valid | Reused as-is — **no seat is consumed**, `/activate` is never called |
| No stored activation, or the server rejected it | `POST /api/v1/licenses/activate` with `{licenseKey, machineId, productSku, osInfo}`, and the token is written to `state-dir` |
| Activation succeeds | Token stored in `LicenseHolder` in memory; `valid = true` |
| Activation fails | Application context startup is aborted (hard fail) |
| Server unreachable at startup, stored activation present | App starts on the stored activation and serves traffic for the grace period |
| Every request | `LicenseValidationFilter` checks `LicenseHolder.isValid()` |
| Periodically | `LicenseVerificationScheduler` calls `POST /api/v1/licenses/verify` on the configured interval |
| Server unreachable during verify | Grace period allows traffic for N minutes after last successful check |
| License invalid / expired | Returns HTTP 503 JSON: `{"status": false, "messages": ["License invalid or expired"]}` |

---

## Seats

A seat is consumed by `/activate`, and only the first time a given machine id is seen. The client
therefore activates once per installation and re-uses that activation on every later start.

Two things keep the seat count flat:

1. **The machine id is persistent.** It is resolved once and written to `<state-dir>/machine-id`.
2. **The activation token is cached.** It is written to `<state-dir>/activation-<hash>.json` and
   re-verified at startup instead of being re-issued.

Delete `state-dir` only when you intend to register the machine again.

In Docker, CI, or anywhere the filesystem is discarded between runs, mount `state-dir` on a volume
or pin the id explicitly:

```properties
geolicense.machine-id=geobill-prod-01
```

## Machine ID

Resolution order, first match wins:

1. `geolicense.machine-id` from configuration.
2. `<state-dir>/machine-id`, written by an earlier run.
3. `~/.geolicense-machine-id`, written by releases up to 1.0.2 (kept so existing seats survive the upgrade).
4. A SHA-256 hash of every physical MAC address, sorted so interface enumeration order cannot change it.
   Loopback, virtual, and point-to-point interfaces are skipped, as are `awdl*`, `llw*`, `utun*`,
   `bridge*`, `docker*`, `vmnet*` and friends, and any locally administered (randomised) MAC.
5. A random UUID.

Whatever is resolved in steps 4 and 5 is persisted immediately, so it never changes again.

> Randomised interfaces are the reason this matters. On macOS `awdl0` and `llw0` rotate their MAC on
> their own, and Wi-Fi private addressing rotates `en1`. A generator that picks the first interface
> it finds returns a different id every few restarts, and each one costs a seat.

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
| `geolicense.state-dir` | `${user.home}/.geolicense` | Where the machine id and the cached activation token are stored |
| `geolicense.machine-id` | — | Pins the machine id; overrides hardware detection. Use it on containers and CI |
