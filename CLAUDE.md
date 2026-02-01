# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TicketZ is a ticket management system built with Spring Boot 3.3 and Java 22, implementing **hexagonal architecture (Ports & Adapters)**. It provides both a server-side rendered web interface (Thymeleaf) and a REST API with JWT authentication.

## Build and Test Commands

```bash
mvn spring-boot:run                    # Run the application
mvn test                               # Run all tests
mvn test -Dtest=ClassName              # Run a single test class
mvn test -Dtest=ClassName#methodName   # Run a single test method
```

**Application URLs:**
- Web UI: `http://localhost:8080/ticketz/index`
- REST API: `http://localhost:8080/ticketz/api/*`
- Test credentials: `test@us.er` / `test`

## Architecture

### Hexagonal Architecture Layers

```
src/main/java/net/wickedshell/ticketz/
├── adapter/              # Outer layer (infrastructure)
│   ├── jpa/              # Persistence adapter (entities, repositories)
│   ├── rest/             # REST API adapter (controllers, DTOs)
│   └── web/              # Web UI adapter (controllers, Thymeleaf)
├── service/              # Core layer (business logic)
│   ├── model/            # Domain models
│   └── port/             # Port interfaces (access + persistence)
```

**Key principle:** Dependencies point inward. Adapters depend on ports, never the reverse.

### Domain Verticals

1. **User Domain** - Email-based authentication, RBAC (ROLE_USER, ROLE_ADMIN, ROLE_API)
2. **Ticket Domain** - State machine: CREATED → IN_PROGRESS → FIXED/REJECTED, optimistic locking
3. **Comment Domain** - Immutable audit trail for tickets
4. **Project Domain** - Organizes tickets, uses unique codes as identifiers

## Critical Architecture Rules

### Domain Models Must NOT Contain Database IDs

Domain models in `service/model/` use **business identifiers only**:
- `User` → `email` (not `id`)
- `Ticket` → `ticketNumber` (not `id`)
- `Project` → `code` (not `id`)

Database IDs belong exclusively to JPA entities in `adapter/jpa/entity/`.

### Timestamp Management

Use Hibernate annotations on entities, **not** manual setting in services:
```java
@CreationTimestamp
@Column(nullable = false, updatable = false)
private LocalDateTime dateCreated;

@UpdateTimestamp
@Column(nullable = false)
private LocalDateTime dateUpdated;
```

### Validation Strategy

- Use Jakarta Bean Validation annotations on domain models (`@NotBlank`, `@Size`, `@Pattern`)
- Trigger validation with `@Valid` on service method parameters
- Business rules (uniqueness, state transitions) go in service layer

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| JPA Entity | `*Entity` | `TicketEntity`, `UserEntity` |
| Domain Model | Plain name | `Ticket`, `User` |
| Service | `*ServiceImpl` | `TicketServiceImpl` |
| Port Interface | `*Service`, `*Persistence` | `TicketService`, `TicketPersistence` |
| Persistence Impl | `*JPAPersistenceImpl` | `TicketJPAPersistenceImpl` |
| REST Controller | `Rest*Controller` | `RestTicketController` |
| Web Controller | `*Controller` | `TicketController` |
| Test Methods | `test{Method}_{scenario}` | `testCreate_validTicket` |

## Security Model

- **Web adapter:** Session-based authentication with CSRF protection
- **REST adapter:** JWT token authentication (stateless)
- **Authorization:** `@PreAuthorize` and `@PostAuthorize` annotations
- **Passwords:** BCrypt hashing

## Solution Design Process

Before implementing significant features, create a design document:
1. Copy `docs/solution-design/TEMPLATE.md` to `docs/solution-design/YYYY-MM-DD-feature-name.md`
2. Fill in all sections (problem, goals, architecture impact, implementation phases)
3. Mark status as "Draft" initially

## Adding New Features

1. **Domain Model** → `service/model/` with validation annotations
2. **Port Interface** → `service/port/access/` and/or `service/port/persistence/`
3. **Service Implementation** → `service/*ServiceImpl.java`
4. **Persistence** → Entity in `adapter/jpa/entity/`, repository, persistence impl
5. **Adapter** → REST and/or Web controllers with DTOs
6. **Tests** → JPA and REST integration tests (mandatory); service unit tests only for high-value business logic
