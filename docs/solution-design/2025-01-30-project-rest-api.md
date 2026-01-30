# Project REST API Endpoints

**Date**: 2025-01-30
**Author**: AI Assistant (Claude Code)
**Status**: Draft

---

## Problem Statement

The Project domain has been implemented at the service and persistence layers, but there is no way for external API clients to manage projects. REST API clients cannot:

- Create new projects
- Retrieve project details
- List all projects
- Update project information (name, description, active status)

This limits the usefulness of the Project feature for integrations and automation.

## Goals

- Expose Project CRUD operations via REST API
- Follow existing REST adapter patterns (authentication, DTOs, error handling)
- Maintain consistency with existing `/api/tickets` endpoint design
- Require `ROLE_API` authorization for all endpoints

## Non-Goals

- Project deletion endpoint (projects can only be deactivated per original design)
- Filtering or pagination for project list (can be added later)
- Project-specific ticket listing (use existing ticket endpoints with filtering)
- Bulk operations

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- [ ] Web [x] REST [ ] Persistence
- [ ] TicketService [ ] UserService [ ] CommentService [x] ProjectService
- [x] New components: RestProjectController, ProjectRest DTO

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core (no changes to service layer)
- [x] New ports/adapters properly separated (REST adapter only)
- [x] No framework dependencies in domain layer

### Design Details

**REST DTO:**

```java
package net.wickedshell.ticketz.adapter.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRest {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$")
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private boolean active;

    private Long version;
}
```

**API Endpoints:**

| Method | Path | Description | Request Body | Response |
|--------|------|-------------|--------------|----------|
| GET | `/api/projects` | List all projects | - | 200 + `List<ProjectRest>` |
| GET | `/api/projects/{code}` | Get project by code | - | 200 + `ProjectRest` / 404 |
| POST | `/api/projects` | Create new project | `ProjectRest` | 201 + Location header |
| PUT | `/api/projects/{code}` | Update project | `ProjectRest` | 204 / 400 / 404 / 409 |

**Controller Implementation:**

```java
package net.wickedshell.ticketz.adapter.rest.controller;

@RequiredArgsConstructor
@RestController
@RequestMapping(RestRessource.RESOURCE_PROJECTS)
public class RestProjectController {

    private final ProjectService projectService;
    private final ModelMapper mapper = new ModelMapper();

    @GetMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<List<ProjectRest>> allProjects() {
        List<ProjectRest> projects = projectService.listAll()
                .stream()
                .map(project -> mapper.map(project, ProjectRest.class))
                .toList();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<ProjectRest> oneProject(@PathVariable("code") String code) {
        Project project = projectService.loadByCode(code)
                .orElseThrow(() -> new ObjectNotFoundException("Project not found: " + code));
        return ResponseEntity.ok(mapper.map(project, ProjectRest.class));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<ProjectRest> create(@RequestBody ProjectRest projectRest) {
        Project newProject = projectService.create(mapper.map(projectRest, Project.class));
        return ResponseEntity
                .created(URI.create(RestRessource.RESOURCE_PROJECTS + "/" + newProject.getCode()))
                .build();
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_API')")
    public ResponseEntity<Void> update(@PathVariable("code") String code,
                                       @RequestBody ProjectRest projectRest) {
        if (!code.equals(projectRest.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        projectService.update(mapper.map(projectRest, Project.class));
        return ResponseEntity.noContent().build();
    }
}
```

**Resource Constant:**

Add to `RestRessource.java`:
```java
public static final String RESOURCE_PROJECTS = "/api/projects";
```

**Error Responses:**

Handled by existing `RestExceptionAdvice`:

| Scenario | Exception | HTTP Status |
|----------|-----------|-------------|
| Project not found | `ObjectNotFoundException` | 404 |
| Duplicate code | `DataIntegrityViolationException` | 409 |
| Concurrent update | `OptimisticLockException` | 409 |
| Validation failure | `ValidationException` | 400 |
| Unauthorized | `AccessDeniedException` | 401 |

### Implementation Approach

**Phase 1: DTO and Resource Constant**
1. Create `ProjectRest` DTO in `adapter/rest/model/`
2. Add `RESOURCE_PROJECTS` constant to `RestRessource`

**Phase 2: Controller**
1. Create `RestProjectController` in `adapter/rest/controller/`
2. Implement all four endpoints following `RestTicketController` patterns

**Phase 3: Testing**
1. Create `RestProjectControllerTest` integration test
2. Test all endpoints with authentication
3. Test error scenarios (404, 409, 400)

---

## Alternatives Considered

### Alternative 1: Use Database ID in URL Path

**Description**: Use `/api/projects/{id}` instead of `/api/projects/{code}`

**Pros:**
- Consistent with some REST conventions
- Simpler lookup (primary key)

**Cons:**
- Exposes internal database IDs
- Inconsistent with domain model design (code is the business identifier)
- Less readable URLs
- IDs may change across environments

**Decision**: Rejected. Use business identifier (code) consistent with hexagonal architecture principles and existing ticket endpoint pattern (uses ticketNumber).

### Alternative 2: Separate Endpoints for Activation/Deactivation

**Description**: Add `POST /api/projects/{code}/activate` and `POST /api/projects/{code}/deactivate`

**Pros:**
- More explicit intent
- Simpler request body for status changes

**Cons:**
- Inconsistent with existing patterns (TicketController uses PUT for all updates)
- More endpoints to maintain
- Can achieve same result with PUT

**Decision**: Rejected. Use single PUT endpoint for all updates, consistent with existing patterns.

---

## Testing Strategy

### Unit Tests

Not applicable - controller is thin adapter, business logic tested at service layer.

### Integration Tests

**RestProjectControllerTest:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class RestProjectControllerTest {

    @Test
    @WithMockUser(roles = "API")
    void testAllProjects_returnsProjectList() { }

    @Test
    @WithMockUser(roles = "API")
    void testOneProject_found() { }

    @Test
    @WithMockUser(roles = "API")
    void testOneProject_notFound_returns404() { }

    @Test
    @WithMockUser(roles = "API")
    void testCreate_success_returns201WithLocation() { }

    @Test
    @WithMockUser(roles = "API")
    void testCreate_duplicateCode_returns409() { }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_success_returns204() { }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_codesMismatch_returns400() { }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_notFound_returns404() { }

    @Test
    @WithMockUser(roles = "API")
    void testUpdate_concurrentModification_returns409() { }

    @Test
    @WithMockUser(roles = "USER")
    void testAllProjects_withoutApiRole_returns401() { }
}
```

### Manual Testing

1. **Authentication**: Verify JWT token required for all endpoints
2. **Create project**: POST with valid data, verify 201 and Location header
3. **List projects**: GET all, verify new project appears
4. **Get single project**: GET by code, verify response matches
5. **Update project**: PUT with changed name/description, verify 204
6. **Deactivate project**: PUT with `active: false`, verify update persists
7. **Error cases**: Test 404, 409, 400 scenarios

---

## Security Considerations

**Authentication:**
- [x] JWT token requirements (existing filter applies)
- [ ] Web session handling (not applicable)
- [ ] No authentication changes

**Authorization:**
- [ ] New roles required
- [ ] Existing role changes
- [x] Permission checks needed at: Controller level (`@PreAuthorize("hasRole('ROLE_API')")`)

**Data Protection:**
- [ ] Sensitive data handling (project data is not sensitive)
- [x] Input validation requirements (Bean Validation on DTO)
- [ ] Output sanitization (no user-generated HTML content)

**CSRF:**
- [ ] Web forms protected (not applicable)
- [x] API endpoints stateless (JWT authentication)

---

## Migration Plan

### Database Migrations

None required - no schema changes.

### Backward Compatibility

- [x] API version unchanged (new endpoints only)
- [x] Existing endpoints unaffected
- [ ] Deprecated features marked

### Deployment Steps

1. Deploy updated application
2. Verify new endpoints available
3. Update API documentation

### Rollback Plan

Remove `RestProjectController` and `ProjectRest` classes, redeploy.

---

## Dependencies

**External Libraries:**
- None (uses existing dependencies)

**Internal Components:**
- `ProjectService` (existing)
- `ModelMapper` (existing)
- `RestExceptionAdvice` (existing error handling)
- JWT authentication filter (existing)

---

## Documentation Updates

- [ ] Update `docs/architecture.md`
- [x] Update API documentation (this document serves as reference)
- [ ] Update `agent.md` if needed
- [x] Add inline code documentation (Javadoc on controller methods)

---

## Open Questions

None - design follows established patterns.

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-01-30 | Use project code in URL path | Consistent with business identifier principle; matches ticket endpoint pattern |
| 2025-01-30 | Single PUT for all updates | Consistent with RestTicketController; simpler API surface |
| 2025-01-30 | No DELETE endpoint | Per original design, projects can only be deactivated |
| 2025-01-30 | Require ROLE_API for all endpoints | Consistent with existing REST security model |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2025-01-30 | Draft | Initial design document created |
