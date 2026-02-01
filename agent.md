# TicketZ - AI Agent Context

## Project Overview

**TicketZ** is a ticket management system built with Spring Boot, implementing hexagonal architecture (Ports & Adapters pattern). The application provides both a web interface and REST API for managing tickets, users, and comments.

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Build Tool**: Maven
- **Database**: H2 (in-memory/file-based)
- **ORM**: JPA/Hibernate
- **Template Engine**: Thymeleaf
- **Security**: Spring Security (sessions + JWT)
- **Testing**: JUnit 5, Mockito, Spring Test

## Project Structure

```
ticketz/
├── src/main/java/net/wickedshell/ticketz/
│   ├── adapter/              # Adapters (outer layer)
│   │   ├── jpa/             # Database persistence adapter
│   │   ├── rest/            # REST API adapter
│   │   └── web/             # Web UI adapter
│   ├── service/             # Application services (core layer)
│   │   ├── model/           # Domain models
│   │   └── port/            # Port interfaces
│   └── TicketzApplication.java
├── src/main/resources/
│   ├── templates/           # Thymeleaf templates
│   ├── static/              # CSS, JS, images
│   └── application.properties
├── src/test/java/           # Unit and integration tests
└── docs/                    # Project documentation
    ├── architecture.md      # Architecture documentation
    ├── README.md           # Documentation index
    └── solution-design/    # Solution design documents (temporary)
```

## Architecture Principles

### Hexagonal Architecture (Ports & Adapters)

1. **Application Core**: Business logic independent of frameworks
   - Domain models (User, Ticket, Comment)
   - Application services (UserServiceImpl, TicketServiceImpl, CommentServiceImpl)
   - Port interfaces defining contracts

2. **Primary Adapters (Driving)**: Interfaces for external actors
   - Web adapter: Server-side rendered UI
   - REST adapter: RESTful API with JWT authentication

3. **Secondary Adapters (Driven)**: Infrastructure implementations
   - Persistence adapter: JPA repositories and entities
   - Security adapter: Authentication and authorization

### Key Design Patterns

- **Repository Pattern**: Data access abstraction
- **Dependency Injection**: IoC via Spring
- **DTO Pattern**: Separate models for each adapter
- **Port/Adapter Pattern**: Clean layer separation

## Domain Model

### Business Verticals

1. **User Domain**
   - User management and authentication
   - Role-based access control (ROLE_USER, ROLE_ADMIN, ROLE_API)
   - Dual authentication: sessions (web) + JWT (API)

2. **Ticket Domain**
   - Ticket lifecycle management
   - State transitions: CREATED → IN_PROGRESS → FIXED/REJECTED
   - Version control with optimistic locking
   - Mandatory project assignment

3. **Comment Domain**
   - Immutable audit trail for tickets
   - Author tracking and chronological ordering

4. **Project Domain**
   - Project organization for tickets
   - Unique project codes as business identifiers
   - Active/inactive status (inactive projects prevent ticket modifications)

### Domain Model Rules

**CRITICAL**: Domain models (`service/model/*`) must NEVER contain database IDs.

- ✅ **Correct**: Domain models use business identifiers (e.g., `ticketNumber`, `email`, `code`)
- ❌ **Wrong**: Domain models with `id` or `ID` fields (that belongs to entities only)
- **Rationale**: Database IDs are infrastructure concerns and violate hexagonal architecture principles

**Examples**:
- `User` uses `email` as identifier (no `id` field)
- `Ticket` uses `ticketNumber` as identifier (no `id` field)
- `Project` uses `code` as identifier (no `id` field)
- `UserEntity`, `TicketEntity`, `ProjectEntity` have `id` fields (persistence layer only)

## Code Style Guidelines

### Java Conventions

- Use Lombok annotations (@Data, @RequiredArgsConstructor, @Getter, @Setter)
- Use Jakarta Bean Validation annotations for domain model validation (@NotBlank, @Size, @Pattern, etc.)
- Use @Valid annotation on service method parameters to trigger validation
- Follow Spring Boot best practices
- Package by feature/layer (adapter, service, model)
- Use meaningful variable and method names
- Keep methods focused and single-purpose

### Naming Conventions

- **Entities**: `*Entity` (e.g., `TicketEntity`, `UserEntity`)
- **DTOs**: Context-specific (e.g., `TicketRest`, `TicketWeb`, `LoginRequest`)
- **Services**: `*ServiceImpl` implementing `*Service` interface
- **Controllers**: `*Controller` (e.g., `TicketController`, `RestTicketController`)
- **Repositories**: `*Repository` (Spring Data JPA)
- **Persistence**: `*JPAPersistenceImpl` implementing `*Persistence` interface

### File Organization

- Controllers in `adapter/{web|rest}/controller/`
- Services in `service/`
- Port interfaces in `service/port/`
- Domain models in `service/model/`
- JPA entities in `adapter/jpa/entity/`
- Repositories in `adapter/jpa/repository/`

## Development Guidelines

### Adding New Features

1. **Define Domain Model**: Add/modify entities in `service/model/` with Bean Validation annotations
2. **Create Port Interface**: Define contract in `service/port/` with @Valid on parameters
3. **Implement Service**: Business logic in `service/*ServiceImpl` with @Valid on method parameters
4. **Add Persistence**: Create entity, repository, and persistence implementation
5. **Create Adapters**: Add web and/or REST controllers
6. **Write Tests**: JPA and REST integration tests are mandatory; service unit tests only for high-value business logic (use `test{MethodName}_{scenario}` naming)

### Validation Strategy

- **Domain Model Validation**: Use Jakarta Bean Validation annotations (@NotBlank, @Size, @Pattern, @Email, etc.)
- **Business Rule Validation**: Implement in service layer (e.g., uniqueness checks, state transitions)
- **Trigger Validation**: Use @Valid annotation on service method parameters
- **Avoid**: Programmatic validation that duplicates Bean Validation constraints

### Timestamp Management

- **Automatic Timestamps**: Use Hibernate annotations for audit fields
  - `@CreationTimestamp` for creation timestamps (with `updatable = false`)
  - `@UpdateTimestamp` for last modified timestamps
- **Layer Responsibility**: Timestamp management belongs to the **persistence layer** (JPA entities)
- **Service Layer**: Do NOT manually set `dateCreated` or `dateUpdated` in service implementations
- **Example**:
  ```java
  @Entity
  public class MyEntity {
      @Column(nullable = false, updatable = false)
      @CreationTimestamp
      private LocalDateTime dateCreated;
      
      @Column(nullable = false)
      @UpdateTimestamp
      private LocalDateTime dateUpdated;
  }
  ```

### Security

- Web: session-based authentication, CSRF protection
- REST API: JWT token-based authentication
- Passwords: BCrypt hashing
- Authorization: @PreAuthorize and @PostAuthorize annotations

## Common Tasks

### Running the Application

```bash
mvn spring-boot:run    # Run application
mvn test              # Run tests
```

### Adding Features

**REST Endpoint**: Create DTO → Update service interface/impl → Add controller method → Write tests

**Web Page**: Create Thymeleaf template → Add controller method → Update navigation in `fragments/header.html`

## Key Design Decisions

- **Dual Authentication**: Web (sessions) + REST API (JWT) for different use cases
- **Immutable Comments**: Audit trail integrity (no edits/deletes)
- **Optimistic Locking**: Version field prevents concurrent update conflicts
- **Email as Username**: Unique email identification
- **H2 Database**: In-memory with `import.sql` seeding

## Critical Components (Avoid Breaking)

- Security filter chain order
- JWT token validation logic
- ModelMapper bean configurations

## Solution Design Documents

### Purpose

Create solution design documents in `docs/solution-design/` before implementing significant features or architectural changes. Use `TEMPLATE.md` as the starting point.

### When to Create

- New features affecting multiple layers
- Architectural changes or refactorings  
- API design or database schema changes
- Security-related modifications

### Quick Reference

- **Template**: `docs/solution-design/TEMPLATE.md` (copy and fill in)
- **Naming**: `YYYY-MM-DD-feature-name.md`
- **Lifecycle**: Draft → Under Review → Approved → Implemented
- **Retention**: Keep all documents (including rejected) for historical context

### AI Agent Behavior

When asked to design a feature:

1. Copy `docs/solution-design/TEMPLATE.md` to `docs/solution-design/YYYY-MM-DD-feature-name.md`
2. Fill in all sections from the template
3. Ensure hexagonal architecture compliance
4. Include security and testing implications
5. Mark status as "Draft" initially

## Reference Documentation

- **Architecture**: `docs/architecture.md`
- **Domain Model**: `service/model/` package
- **Database Schema**: `adapter/jpa/entity/` package
- **Solution Design Template**: `docs/solution-design/TEMPLATE.md`

## AI Agent Instructions

When assisting with this project:

1. **Respect Architecture**: Keep business logic in services, adapters at edges
2. **Maintain Separation**: Don't mix web and REST concerns
3. **Follow Patterns**: Use existing patterns for new features
4. **Test Coverage**: Include tests with new code
5. **Security First**: Consider authentication/authorization implications
6. **Update Documentation**: Keep docs current with architectural changes
