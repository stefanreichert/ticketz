# Project Management and Ticket Assignment

**Date**: 2025-10-21  
**Author**: AI Assistant (GitHub Copilot)  
**Status**: Draft

---

## Problem Statement

Currently, tickets in the TicketZ application exist independently without any organizational grouping mechanism. There is no way to group related tickets under a common project or initiative, making it difficult to:

- Track tickets related to a specific project or initiative
- Filter and report on tickets by project
- Understand the scope and context of tickets within larger initiatives
- Organize work across multiple projects

## Goals

- Introduce a Project business entity to organize tickets
- Enable ticket-to-project assignment (mandatory relationship)
- Create ProjectService for project lifecycle management
- Maintain hexagonal architecture principles (business service only, no UI/API in this phase)
- Ensure unique project codes for reliable identification
- Support active/inactive status for project lifecycle management
- Prevent modifications to inactive projects (no new tickets, no editing/deletion of tickets)

## Non-Goals

- Web UI for project management (future phase)
- REST API endpoints for projects (future phase)
- Project hierarchies or nested projects
- User-to-project assignment or permissions
- Project dashboards or analytics
- Ticket reassignment between projects (v1)
- Multi-project support for tickets (single project per ticket only)
- Project deletion functionality (projects can only be deactivated)

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- [x] Web [ ] REST [x] Persistence
- [ ] TicketService [x] UserService [ ] CommentService
- [x] New components: ProjectService, Project domain model

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core (service layer)
- [x] New ports/adapters properly separated (ProjectService port + JPA persistence adapter)
- [x] No framework dependencies in domain layer

### Design Details

**Domain Model Changes:**

```java
// New domain model in service/model/
package net.wickedshell.ticketz.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Project {
    // NOTE: No 'id' field - domain models use business identifiers only
    
    @NotBlank(message = "Project code is required")
    @Size(max = 50, message = "Project code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Project code must contain only letters, numbers, hyphens, and underscores")
    private String code;           // Unique business identifier (e.g., "PROJ-001", "WEBAPP")
    
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;           // Display name (e.g., "Web Application Rewrite")
    
    @Size(max = 1000, message = "Project description must not exceed 1000 characters")
    private String description;    // Optional project description
    
    private boolean active;        // Active/inactive status (inactive projects cannot be modified)
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Long version;          // For optimistic locking
}

// Updated Ticket domain model
@Data
public class Ticket {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketState state;
    private User author;
    private User editor;
    private Project project;       // NEW: Mandatory project assignment
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Long version;
}
```

**Port Interface:**

```java
// New port interface in service/port/access/
package net.wickedshell.ticketz.service.port.access;

import net.wickedshell.ticketz.service.model.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectService {
    /**
     * Create a new project.
     * @throws IllegalArgumentException if code is null, empty, or already exists
     */
    Project create(Project project);
    
    /**
     * Update an existing project.
     * @throws NotFoundException if project doesn't exist
     * @throws IllegalArgumentException if code already exists (on different project)
     */
    Project update(Project project);
    
    /**
     * Load project by unique code.
     */
    Optional<Project> loadByCode(String code);
    
    /**
     * Load project by ID.
     */
    Optional<Project> loadById(Long id);
    
    /**
     * List all projects.
     */
    List<Project> listAll();
    
    /**
     * Set project as active.
     */
    void setActive(Long id);
    
    /**
     * Set project as inactive.
     * Inactive projects cannot have new tickets or ticket modifications.
     */
    void setInactive(Long id);
    
    /**
     * Check if project code is unique.
     */
    boolean isCodeUnique(String code);
}
```

**Service Implementation:**

```java
// New service in service/
package net.wickedshell.ticketz.service;

@RequiredArgsConstructor
@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectPersistence projectPersistence;
    
    @Override
    public Project create(Project project) {
        validateProject(project);
        validateCodeUniqueness(project.getCode(), null);
        return projectPersistence.save(project);
    }
    
    @Override
    public Project update(Project project) {
        validateProject(project);
        validateCodeUniqueness(project.getCode(), project.getId());
        return projectPersistence.update(project);
    }
    
    private void validateProject(Project project) {
        if (project.getCode() == null || project.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Project code is required");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        // Code validation: alphanumeric, hyphens, underscores
        if (!project.getCode().matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("Project code must contain only letters, numbers, hyphens, and underscores");
        }
    }
    
    private void validateCodeUniqueness(String code, Long excludeId) {
        Optional<Project> existing = projectPersistence.loadByCode(code);
        if (existing.isPresent() && !existing.get().getId().equals(excludeId)) {
            throw new IllegalArgumentException("Project code '" + code + "' already exists");
        }
    }
    
    // ... other methods
}
```

**Database Schema:**

```sql
-- New table: PROJECT_ENTITY
CREATE TABLE PROJECT_ENTITY (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(1000),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_project_code ON PROJECT_ENTITY(code);

-- Update table: TICKET_ENTITY
ALTER TABLE TICKET_ENTITY 
ADD COLUMN project_id BIGINT NOT NULL;

ALTER TABLE TICKET_ENTITY
ADD CONSTRAINT fk_ticket_project 
FOREIGN KEY (project_id) REFERENCES PROJECT_ENTITY(id);

CREATE INDEX idx_ticket_project ON TICKET_ENTITY(project_id);
```

**JPA Entity:**

```java
// New entity in adapter/jpa/entity/
package net.wickedshell.ticketz.adapter.jpa.entity;

@Data
@Entity
public class ProjectEntity {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateUpdated;
    
    @Version
    private Long version;
}

// Updated TicketEntity
@Data
@Entity
public class TicketEntity {
    // ... existing fields
    
    @ManyToOne(optional = false)  // NEW: Mandatory relationship
    private ProjectEntity project;
    
    // ... rest of fields
}
```

### Implementation Approach

**Phase 1: Core Project Domain (2-3 days)**
1. Create Project domain model in `service/model/Project.java`
2. Create ProjectService port interface in `service/port/access/ProjectService.java`
3. Create ProjectServiceImpl in `service/ProjectServiceImpl.java`
4. Add validation logic (code uniqueness, format validation)

**Phase 2: Persistence Layer (2 days)**
1. Create ProjectEntity in `adapter/jpa/entity/ProjectEntity.java`
2. Create ProjectRepository in `adapter/jpa/repository/ProjectRepository.java`
3. Create ProjectPersistence port in `service/port/driven/ProjectPersistence.java`
4. Create ProjectJPAPersistenceImpl in `adapter/jpa/persistence/ProjectJPAPersistenceImpl.java`
5. Create model mappers (Project ↔ ProjectEntity)

**Phase 3: Update Ticket Domain (2 days)**
1. Add project field to Ticket domain model
2. Update TicketEntity with project relationship
3. Update TicketServiceImpl to handle project assignment
4. Ensure all ticket operations consider project context

**Phase 4: Data Migration (1 day)**
1. Create database migration script
2. Create default project for existing tickets
3. Test migration on test dataset

**Estimated Effort**: 7-8 days

---

## Alternatives Considered

### Alternative 1: Project as Optional on Tickets

**Description**: Make project assignment optional on tickets, allowing tickets without projects.

**Pros:**
- Easier migration (no default project needed)
- More flexible for ad-hoc tickets
- Gradual adoption possible

**Cons:**
- Inconsistent data model (some tickets with, some without projects)
- More complex querying and filtering logic
- Doesn't enforce organizational structure
- Future reporting and analytics become complicated

**Decision**: Rejected. Mandatory project assignment enforces better organization from the start and simplifies future features.

### Alternative 2: Use Project Name as Unique Identifier

**Description**: Use project name instead of separate code for uniqueness.

**Pros:**
- Simpler model (one less field)
- Users don't need to think about codes
- More user-friendly

**Cons:**
- Names are verbose for URLs and displays
- Names may need to change over time (e.g., rebranding)
- Codes provide stable, short identifiers for integration
- Name changes would break historical references

**Decision**: Rejected. Separate code provides stable identifier while allowing name flexibility.

### Alternative 3: Project Hierarchies

**Description**: Support parent-child relationships between projects.

**Pros:**
- Supports complex organizational structures
- Better reflects real-world project organization
- Enables roll-up reporting

**Cons:**
- Significant complexity increase
- Overkill for current requirements
- Can be added later if needed
- More difficult to implement and maintain

**Decision**: Rejected for v1. Keep it simple initially, add hierarchies later if demand exists.

---

## Testing Strategy

### Unit Tests

**ProjectServiceImplTest:**
- `testCreate_success()` - Valid project creation
- `testCreate_fail_duplicateCode()` - Unique code constraint
- `testCreate_fail_emptyCode()` - Code validation
- `testCreate_fail_emptyName()` - Name validation
- `testCreate_fail_invalidCodeFormat()` - Code format validation
- `testUpdate_success()` - Update existing project
- `testUpdate_fail_codeConflict()` - Code uniqueness on update
- `testLoadByCode_found()` - Retrieve by code
- `testLoadByCode_notFound()` - Handle missing project
- `testDeleteById_success()` - Delete unused project
- `testDeleteById_fail_hasTickets()` - Prevent deletion with assigned tickets

**TicketServiceImplTest:**
- `testCreate_withProject()` - Ticket creation with project assignment
- `testUpdate_changeProject()` - Project reassignment (if supported)
- `testLoadByProject()` - Filter tickets by project

### Integration Tests

**ProjectJPAPersistenceImplTest:**
- Test CRUD operations with database
- Verify unique constraint on code
- Test optimistic locking with version field
- Verify cascade behavior with tickets

**TicketJPAPersistenceImplTest:**
- Test ticket-project relationship persistence
- Verify foreign key constraint enforcement
- Test querying tickets by project

### Manual Testing

1. **Project Creation:**
   - Create project with valid code and name
   - Attempt duplicate code (should fail)
   - Attempt invalid code format (should fail)

2. **Ticket Assignment:**
   - Create ticket with project assignment
   - Verify ticket loads with project relationship
   - Attempt ticket creation without project (should fail)

3. **Data Integrity:**
   - Attempt to delete project with assigned tickets (should fail)
   - Delete project without tickets (should succeed)

---

## Security Considerations

**Authentication:**
- [x] Web session handling
- [x] JWT token requirements
- [ ] No authentication changes (business service only in this phase)

**Authorization:**
- [ ] New roles required
- [ ] Existing role changes
- [ ] Permission checks needed at: Service layer (future phase)

**Data Protection:**
- [ ] Sensitive data handling (projects are not sensitive)
- [x] Input validation requirements (code format, uniqueness)
- [ ] Output sanitization (no user-facing components yet)

**CSRF:**
- [ ] Web forms protected (no UI in this phase)
- [ ] API endpoints stateless (no API in this phase)

**Notes:**
- Project codes and names are non-sensitive organizational data
- Authorization checks will be added when UI/API adapters are implemented
- Current phase focuses on business logic integrity

---

## Migration Plan

### Database Migrations

```sql
-- Migration Script: V1__add_project_support.sql

-- Step 1: Create PROJECT_ENTITY table
CREATE TABLE PROJECT_ENTITY (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    date_created TIMESTAMP NOT NULL,
    date_updated TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_project_code ON PROJECT_ENTITY(code);

-- Step 2: Create default project for existing tickets
INSERT INTO PROJECT_ENTITY (code, name, description, date_created, date_updated, version)
VALUES ('DEFAULT', 'Default Project', 'Automatically created for existing tickets', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Step 3: Add project_id column to TICKET_ENTITY (nullable first)
ALTER TABLE TICKET_ENTITY 
ADD COLUMN project_id BIGINT;

-- Step 4: Assign all existing tickets to default project
UPDATE TICKET_ENTITY 
SET project_id = (SELECT id FROM PROJECT_ENTITY WHERE code = 'DEFAULT');

-- Step 5: Make project_id NOT NULL
ALTER TABLE TICKET_ENTITY 
ALTER COLUMN project_id SET NOT NULL;

-- Step 6: Add foreign key constraint
ALTER TABLE TICKET_ENTITY
ADD CONSTRAINT fk_ticket_project 
FOREIGN KEY (project_id) REFERENCES PROJECT_ENTITY(id);

CREATE INDEX idx_ticket_project ON TICKET_ENTITY(project_id);
```

### Backward Compatibility

- [x] API version unchanged (no API changes yet)
- [x] Existing endpoints unaffected (no endpoints affected)
- [ ] Deprecated features marked (no deprecations)

**Notes:**
- Existing tickets automatically assigned to "DEFAULT" project
- No breaking changes to current functionality
- Service layer changes are internal, no external API impact

### Deployment Steps

1. **Pre-deployment:**
   - Review and test migration script on staging environment
   - Backup production database
   - Communicate maintenance window if needed

2. **Database migration:**
   - Execute V1__add_project_support.sql
   - Verify default project created
   - Verify all existing tickets have project assignment

3. **Deploy new code:**
   - Deploy updated application with ProjectService
   - Verify ProjectService bean initialization
   - Check application logs for errors

4. **Post-deployment verification:**
   - Test project creation via test harness
   - Verify ticket creation still works
   - Check existing ticket access
   - Monitor error logs for 24 hours

5. **Update documentation:**
   - Update architecture.md with new Project domain
   - Add Project to business verticals diagram
   - Update agent.md if needed

### Rollback Plan

**If issues occur within first hour:**

1. **Rollback code:**
   ```bash
   # Deploy previous version
   git checkout previous-release-tag
   mvn clean install
   # Redeploy
   ```

2. **Database rollback:**
   ```sql
   -- Remove foreign key constraint
   ALTER TABLE TICKET_ENTITY DROP CONSTRAINT fk_ticket_project;
   
   -- Drop project_id column
   ALTER TABLE TICKET_ENTITY DROP COLUMN project_id;
   
   -- Drop project table
   DROP TABLE PROJECT_ENTITY;
   ```

3. **Verify rollback:**
   - Test ticket creation/retrieval
   - Check application logs
   - Monitor for 30 minutes

**Critical:** Database rollback removes all project data. Document any manually created projects before rollback.

---

## Dependencies

**External Libraries:**
- None (uses existing Spring Boot, JPA, Lombok dependencies)

**Internal Components:**
- TicketService (updated to handle project relationships)
- ModelMapper (for Project ↔ ProjectEntity conversion)
- Existing JPA infrastructure

---

## Documentation Updates

- [x] Update `docs/architecture.md` - Add Project to business verticals diagram
- [ ] Update API documentation (future when API endpoints added)
- [x] Update `agent.md` - Add Project to domain model section
- [x] Add inline code documentation (Javadoc for ProjectService and ProjectServiceImpl)

---

## Open Questions

1. ~~**Should projects have an active/inactive status flag?**~~ **[RESOLVED]**
   - **Decision**: YES - Active/inactive status implemented
   - **Impact**: Projects can be deactivated to prevent modifications
   - **Rule**: Inactive projects cannot have new tickets or ticket edits/deletions

2. ~~**Should we support project deletion or only archival?**~~ **[RESOLVED]**
   - **Decision**: No deletion support - archival only via inactive status
   - **Impact**: Projects remain in database for historical reference
   - **Rule**: Projects can only be deactivated, never deleted

3. **Should project codes be case-sensitive?**
   - Context: "WEBAPP" vs "webapp" - treat as same or different?
   - Impact: Code validation and uniqueness checking
   - Recommendation: Store as entered but enforce case-insensitive uniqueness

4. ~~**Should there be a maximum number of projects?**~~ **[RESOLVED]**
   - **Decision**: No limit on project count
   - **Impact**: No validation logic needed for project count

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-10-21 | Mandatory project assignment on tickets | Enforces organization, simplifies querying and future features |
| 2025-10-21 | Separate code and name fields | Provides stable identifier while allowing name changes |
| 2025-10-21 | Business service only (no UI/API) | Focused iteration, adapters added in subsequent phases |
| 2025-10-21 | Create default project for migration | Ensures data integrity for existing tickets |
| 2025-10-21 | Use optimistic locking (version field) | Consistent with existing Ticket entity pattern |
| 2025-10-21 | Add active/inactive status to projects | Allows project archival without deletion; prevents modifications to inactive projects |
| 2025-10-21 | No project deletion functionality | Projects remain for historical reference; deactivation is sufficient |
| 2025-10-21 | No multi-project support for tickets | Simplifies v1 implementation; single project per ticket only |
| 2025-10-21 | No project count limit | Monitoring usage instead of enforcing arbitrary limits |
| 2025-10-22 | Use Bean Validation for domain constraints | Declarative validation consistent with Ticket domain; eliminates programmatic validation code |
| 2025-10-22 | Timestamp management in persistence layer | Use @CreationTimestamp/@UpdateTimestamp; removes manual timestamp logic from service layer |
| 2025-10-22 | Streamline ProjectService to single update() method | Consistent with TicketService pattern; all updates go through one method instead of individual field setters |
| 2025-10-22 | ProjectPersistence in service.port.persistence package | Consistent with TicketPersistence location; follows established port package structure |
| 2025-10-22 | Use create()/findAll() method names in ProjectPersistence | Consistent with TicketPersistence API naming conventions |
| 2025-10-22 | Single update() method for all field changes | Consistent with TicketService pattern; removed individual field setters (setActive, setInactive); all updates go through one method |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2025-10-21 | Draft | Initial design document created |
| 2025-10-21 | Updated | Added active/inactive status, removed deletion, clarified no multi-project support |
