# Cache-Aside Demo

An educational Spring Boot project demonstrating the **Cache-Aside** caching
pattern with two independent domains, each backed by a different cache:

- **Product** — cached with **Redis** (distributed).
- **Category** — cached with **Caffeine** (local, in-process).

## The Cache-Aside pattern

On a read, the application looks in the cache first:

1. **Cache hit** — return the cached value, skip the database entirely.
2. **Cache miss** — read from the database, store the result in the cache,
   then return it.

On a write (create/update/delete), the application updates the database and
immediately updates (or removes) the corresponding cache entry, so the cache
never serves data that's older than the last write:

```
Read:  App -> Cache (miss) -> DB -> populate Cache -> return
Write: App -> DB -> update/evict Cache
```

The cache is not the source of truth — the database is. The cache exists
purely to avoid hitting the database for data that was recently read.

## Why Spring Cache annotations instead of manual Redis/Caffeine calls

Both `ProductService` and `CategoryService` use `@Cacheable`, `@CachePut`,
and `@CacheEvict` instead of calling the cache client directly. This is more
idiomatic Spring, but it has one consequence worth knowing: on a cache hit,
Spring returns the cached value **without ever running the annotated
method's body** — so a log statement inside that method cannot fire on a
hit, only on a miss.

**Both services log only on cache miss.** If you call an endpoint and see
no `CACHE MISS` line in the console, that call was a cache **hit**.

## Project structure

The project is organized by technical layer, with both domains living side
by side in each layer:

```
src/main/java/com/academy/cacheasside/
├── CacheAssideApplication.java   # @SpringBootApplication, @EnableCaching
├── config/
│   └── CacheConfig.java          # redisCacheManager + caffeineCacheManager beans
├── controller/
│   ├── ProductController.java    # /api/products
│   └── CategoryController.java   # /api/categories
├── service/
│   ├── ProductService.java       # cache-aside via Redis
│   └── CategoryService.java      # cache-aside via Caffeine
├── repository/
│   ├── ProductRepository.java
│   └── CategoryRepository.java
├── entity/
│   ├── Product.java
│   └── Category.java
├── dto/
│   ├── ProductRequest.java
│   └── CategoryRequest.java
└── exception/
    ├── ProductNotFoundException.java
    ├── CategoryNotFoundException.java
    └── GlobalExceptionHandler.java   # maps both *NotFoundException -> 404
```

## Running it

Requires Docker (for Postgres and Redis) and a JDK compatible with Java 25.

```bash
./mvnw spring-boot:run
```

Spring Boot's Docker Compose support starts `postgres` and `redis` from
`compose.yaml` automatically and wires their connection details into the
app — no manual configuration needed. The app listens on
`http://localhost:8080`.

## Product — Redis cache-aside

| Method | Path | Description |
|---|---|---|
| GET | `/api/products` | List all products (never cached) |
| GET | `/api/products/{id}` | Get a product by id (Redis cache-aside) |
| POST | `/api/products` | Create a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

```bash
# Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Keyboard","description":"Mechanical keyboard","price":250}'

# Read it back (watch the console: CACHE MISS on the first call, nothing on the second)
curl http://localhost:8080/api/products/1

# List all products (always hits the database)
curl http://localhost:8080/api/products

# Update it (cache is updated immediately)
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Keyboard Pro","description":"Mechanical keyboard","price":280}'

# Delete it (cache is evicted immediately)
curl -X DELETE http://localhost:8080/api/products/1
```

## Category — Caffeine cache-aside

| Method | Path | Description |
|---|---|---|
| GET | `/api/categories` | List all categories (never cached) |
| GET | `/api/categories/{id}` | Get a category by id (Caffeine cache-aside) |
| POST | `/api/categories` | Create a category |
| PUT | `/api/categories/{id}` | Update a category |
| DELETE | `/api/categories/{id}` | Delete a category |

```bash
# Create a category
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic devices"}'

# Read it back (watch the console: CACHE MISS on the first call, nothing on the second)
curl http://localhost:8080/api/categories/1

# List all categories (always hits the database)
curl http://localhost:8080/api/categories

# Update it (cache is updated immediately)
curl -X PUT http://localhost:8080/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics & Gadgets","description":"Electronic devices and gadgets"}'

# Delete it (cache is evicted immediately)
curl -X DELETE http://localhost:8080/api/categories/1
```

Because Category's cache is local (Caffeine, in-process), restarting the
app clears it immediately — unlike Product's Redis cache, which survives
an app restart as long as the Redis container keeps running. That
difference is the practical reason a distributed cache is preferred once
an application runs on more than one instance.

## Testing

Integration tests use Testcontainers to run real Postgres and Redis
containers (Caffeine needs no container — it's in-process). They cover the
miss-then-hit flow for both domains, write-path invalidation, and the
not-found/validation error paths.

```bash
./mvnw test
```
