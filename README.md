# redis-with-spring-boot

A Spring Boot application demonstrating different Redis usage patterns through three independent, self-contained examples.

## Tech Stack

| | |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.4 |
| Spring Data Redis | Lettuce (included) |
| Redis | 6+ |
| Lombok | 1.18.30 |

---

## Prerequisites

- Java 21
- Maven
- Docker (to run Redis and Redis Insight via the provided Compose file)

---

## Resources Folder

The `resources/` folder at the project root contains two files to get you up and running quickly.

### `docker-compose.yml`

Starts **Redis** and **Redis Insight** (the Redis GUI) together.

```bash
docker compose -f resources/docker-compose.yml up -d
```

| Service | URL |
|---|---|
| Redis | `localhost:6379` |
| Redis Insight | `http://localhost:5540` |

Open Redis Insight in your browser to browse keys, inspect TTLs, and watch cache entries appear in real time as you call the APIs.

### `Redis Controllers.postman_collection.json`

A ready-to-use Postman collection covering all endpoints across the three controllers.

**Import steps:**
1. Open Postman → **Import**
2. Select `resources/Redis Controllers.postman_collection.json`
3. Set the `baseUrl` collection variable to `http://localhost:8811`
4. Run requests in order within each folder

---

## Running the Application

```bash
./mvnw spring-boot:run
```

The server starts on **port 8811** (configured in `application.properties`).

---

## Configuration

`src/main/resources/application.properties`

```properties
server.port=8811

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms
spring.data.redis.lettuce.pool.max-active=10
spring.data.redis.lettuce.pool.max-idle=5
```

---

## Project Structure

```
src/main/java/com/learn.redis/
├── bean/
│   ├── Event.java               # Event POJO (Serializable)
│   └── User.java                # User POJO
├── config/
│   └── RedisConfig.java         # RedisTemplate + CacheManager beans
├── controller/
│   ├── EventController.java     # @Cacheable example
│   ├── TokenController.java     # Manual RedisTemplate (String ops)
│   └── UserCacheController.java # Manual RedisTemplate (Hash ops)
└── service/
    ├── EventService.java        # @Cacheable / @CachePut / @CacheEvict
    ├── TokenService.java        # opsForValue, increment, TTL
    └── UserCacheService.java    # opsForHash
```

---

## Redis Patterns Covered

| Resource | Pattern | Redis Data Structure |
|---|---|---|
| Token | Manual `RedisTemplate` | String |
| User Cache | Manual `RedisTemplate` | Hash |
| Event | `@Cacheable` annotations | String (JSON via CacheManager) |

---

## Resources

### 1. Token — `/api/tokens`

Demonstrates manual `RedisTemplate` with **String operations**, TTL management, and atomic counters.

**Redis key pattern:** `token:{userId}` · **TTL:** 1 hour

#### Endpoints

| Method | URL | Description |
|---|---|---|
| `POST` | `/api/tokens/{userId}` | Store a token with 1-hour TTL |
| `GET` | `/api/tokens/{userId}` | Retrieve token + remaining TTL in seconds |
| `DELETE` | `/api/tokens/{userId}` | Delete a token |
| `GET` | `/api/tokens/{userId}/exists` | Check if a token key exists |
| `POST` | `/api/tokens/{userId}/login-attempt` | Atomically increment login attempt counter |

#### Examples

```bash
# Save a token
POST /api/tokens/user1
{ "token": "abc123xyz" }

# Get token with TTL
GET /api/tokens/user1
→ { "userId": "user1", "token": "abc123xyz", "ttlSeconds": 3542 }

# Check existence
GET /api/tokens/user1/exists
→ { "userId": "user1", "exists": true }

# Increment login attempts (atomic, no race condition)
POST /api/tokens/user1/login-attempt
→ { "userId": "user1", "loginAttempts": 3 }

# Delete
DELETE /api/tokens/user1
```

---

### 2. User Cache — `/api/users/cache`

Demonstrates manual `RedisTemplate` with **Hash operations**, allowing partial field reads and updates without deserializing the whole object.

**Redis key pattern:** `user:{userId}` · **TTL:** 30 minutes

#### Endpoints

| Method | URL | Description |
|---|---|---|
| `POST` | `/api/users/cache` | Store a user as a Redis Hash |
| `GET` | `/api/users/cache/{userId}` | Retrieve all fields of a cached user |
| `GET` | `/api/users/cache/{userId}/email` | Retrieve only the email field |
| `PATCH` | `/api/users/cache/{userId}/role` | Update only the role field |
| `DELETE` | `/api/users/cache/{userId}` | Evict the user from cache |

#### Examples

```bash
# Cache a user
POST /api/users/cache
{ "id": "1", "name": "John Doe", "email": "john@test.com", "role": "ADMIN" }

# Get all fields
GET /api/users/cache/1
→ { "name": "John Doe", "email": "john@test.com", "role": "ADMIN" }

# Get only email (no full deserialization)
GET /api/users/cache/1/email
→ { "userId": "1", "email": "john@test.com" }

# Update only role (other fields untouched)
PATCH /api/users/cache/1/role
{ "role": "MANAGER" }

# Evict
DELETE /api/users/cache/1
```

**Why Hashes?** Each field is stored and accessed independently. Updating a role doesn't require reading, deserializing, modifying, and re-serializing the entire object.

---

### 3. Event Cache — `/events`

Demonstrates Spring's **annotation-based caching** (`@Cacheable`, `@CachePut`, `@CacheEvict`) backed by Redis via `CacheManager`.

**Cache name:** `events` · **Redis key pattern:** `events::{id}` · **TTL:** 10 minutes (configured in `RedisConfig`)

**Pre-seeded events (in-memory):**

| ID | Title | Location | Date |
|---|---|---|---|
| `1` | Spring Boot Conf | Berlin | 2026-05-10 |
| `2` | Redis Summit | London | 2026-06-20 |
| `3` | JavaOne | SF | 2026-09-15 |

#### Endpoints

| Method | URL | Annotation | Description |
|---|---|---|---|
| `GET` | `/events/{id}` | `@Cacheable` | Returns from Redis on cache hit, skips method on subsequent calls |
| `POST` | `/events` | `@CachePut` | Always saves + updates cache entry |
| `DELETE` | `/events/{id}` | `@CacheEvict` | Deletes event and removes its cache entry |
| `DELETE` | `/events/cache` | `@CacheEvict(allEntries=true)` | Clears the entire `events` cache |

#### Examples

```bash
# First call — hits in-memory DB, server logs ">> DB hit for event: 1"
GET /events/1
→ { "id": "1", "title": "Spring Boot Conf", "location": "Berlin", "date": "2026-05-10" }

# Second call — served from Redis, no log printed
GET /events/1
→ { "id": "1", "title": "Spring Boot Conf", "location": "Berlin", "date": "2026-05-10" }

# Create/update — always writes through to Redis
POST /events
{ "id": "4", "title": "KubeCon", "location": "Paris", "date": "2026-11-05" }

# Delete event + evict its cache entry
DELETE /events/1

# Wipe entire events cache (all IDs)
DELETE /events/cache
```

**How to observe caching:** Watch the server console. The log line `>> DB hit for event: {id}` only prints on a cache miss. On a cache hit, `getEvent()` is never called — Spring returns the value directly from Redis.

---

## Annotation-based vs Manual Caching

| | `@Cacheable` | `RedisTemplate` |
|---|---|---|
| Control | Declarative / automatic | Explicit, full control |
| TTL | Set globally in `CacheManager` | Set per operation |
| Key naming | Spring-generated (`cacheName::key`) | You define it |
| Best for | DB query results, computed data | Tokens, sessions, counters |
| Eviction | `@CacheEvict` | `redisTemplate.delete(key)` |
| Partial updates | Not supported | Yes (`opsForHash`) |

---

## Postman Collection

See the [Resources Folder](#resources-folder) section above — the collection file is located at `resources/Redis Controllers.postman_collection.json`.
