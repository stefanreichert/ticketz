# Project Web UI

**Date**: 2025-02-01
**Author**: AI Assistant (Claude Code)
**Status**: Draft

---

## Problem Statement

The Project domain has been implemented at the service and persistence layers, and exposed via REST API. However, the Thymeleaf web frontend does not provide any UI for project management. Users cannot:

- View a list of projects
- Create new projects
- Edit existing projects (name, description, active status)
- Select a project when creating tickets

Additionally, tickets now require a project assignment, but the ticket creation/edit forms do not support project selection.

## Goals

- Provide a web UI for project CRUD operations (list, create, edit)
- Integrate project selection into ticket creation
- Display project information on ticket views
- Follow existing web adapter patterns and styling

## Non-Goals

- Project deletion (projects can only be deactivated per original design)
- Filtering ticket list by project (future enhancement)
- Project-based user permissions (future enhancement)
- Admin-only project management (all authenticated users can manage projects)

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- [x] Web [ ] REST [ ] Persistence
- [ ] TicketService [ ] UserService [ ] CommentService [x] ProjectService
- [x] New components: ProjectController, ProjectListController, ProjectWeb DTO

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core (no changes to service layer)
- [x] New ports/adapters properly separated (web adapter only)
- [x] No framework dependencies in domain layer

### Design Details

**New Web DTO:**

```java
package net.wickedshell.ticketz.adapter.web.model;

@Data
public class ProjectWeb {
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
    private long version;

    // For new project form
    private boolean isNew;
}
```

**Updated TicketWeb DTO:**

```java
// Add to existing TicketWeb
private String projectCode;
private String projectName;
```

**New Actions:**

```java
// Add to Action.java
public static final String ACTION_SHOW_PROJECT_LIST = "/secure/projects";
public static final String ACTION_NEW_PROJECT = "/secure/projects/new";
public static final String ACTION_SHOW_PROJECT = "/secure/projects/{code}";
public static final String ACTION_SAVE_PROJECT = "/secure/projects/{code}";
```

**New Views:**

```java
// Add to View.java
public static final String VIEW_PROJECT_LIST = "/project_list";
public static final String VIEW_PROJECT = "/project";
```

**URL Structure:**

| Action | URL | Method | Description |
|--------|-----|--------|-------------|
| List projects | `/secure/projects` | GET | Show all projects |
| New project form | `/secure/projects/new` | GET | Empty project form |
| View/edit project | `/secure/projects/{code}` | GET | Project details |
| Save project | `/secure/projects/{code}` | POST | Create or update |

### Implementation Approach

**Phase 1: Project List and Detail Views**

1. Create `ProjectWeb` DTO in `adapter/web/model/`
2. Add view and action constants
3. Create `ProjectListController` with project list population
4. Create `ProjectController` with new/show/save actions
5. Create `project_list.html` template (table with project code, name, status)
6. Create `project.html` template (form for create/edit)
7. Add navigation link to header fragment

**Phase 2: Ticket-Project Integration**

1. Update `TicketWeb` to include `projectCode` and `projectName`
2. Modify `TicketController.newTicket()` to populate available projects
3. Modify `TicketController.saveTicket()` to handle project assignment
4. Update `ticket.html` to show project dropdown for new tickets
5. Update `ticket.html` to display project info for existing tickets
6. Update `ticket_list.html` to show project column

**Phase 3: Messages and Polish**

1. Add i18n messages for project labels and buttons
2. Add success/error messages for project operations
3. Add validation error display

---

## Alternatives Considered

### Alternative 1: Modal-based Project Selection

**Description**: Use a modal dialog for project selection in ticket form instead of a dropdown.

**Pros:**
- More space for project details
- Could include search/filter

**Cons:**
- More complex implementation
- Inconsistent with current form patterns
- Requires additional JavaScript

**Decision**: Rejected. Dropdown is simpler and consistent with existing patterns.

### Alternative 2: Inline Project Creation in Ticket Form

**Description**: Allow creating a new project directly from the ticket creation form.

**Pros:**
- Convenient for users
- Single workflow

**Cons:**
- Adds complexity to ticket form
- Mixed responsibilities
- Can be added later if needed

**Decision**: Rejected for initial implementation. Users should create projects separately.

---

## Testing Strategy

### Unit Tests

Not applicable for web controllers - use integration tests per architecture guidelines.

### Integration Tests

**ProjectListControllerTest:**
- `testShowProjectList_displaysProjects()`
- `testShowProjectList_emptyList()`

**ProjectControllerTest:**
- `testNewProject_showsEmptyForm()`
- `testShowProject_found()`
- `testShowProject_notFound()`
- `testSaveProject_createSuccess()`
- `testSaveProject_updateSuccess()`
- `testSaveProject_validationError()`

### Manual Testing

1. **Project List**: Navigate to projects, verify all projects displayed
2. **Create Project**: Click new, fill form, save, verify in list
3. **Edit Project**: Click project, modify, save, verify changes
4. **Deactivate Project**: Edit project, set inactive, verify ticket creation blocked
5. **Ticket Creation**: Create ticket, verify project dropdown, select project
6. **Ticket Display**: View ticket, verify project info shown

---

## Security Considerations

**Authentication:**
- [x] Web session handling (existing Spring Security config)
- [ ] JWT token requirements
- [ ] No authentication changes

**Authorization:**
- [ ] New roles required
- [ ] Existing role changes
- [x] Permission checks needed at: Controller level (ROLE_USER required via security config)

**Data Protection:**
- [ ] Sensitive data handling
- [x] Input validation requirements (Bean Validation on ProjectWeb)
- [ ] Output sanitization (Thymeleaf auto-escapes)

**CSRF:**
- [x] Web forms protected (Spring Security CSRF enabled)
- [ ] API endpoints stateless

---

## Migration Plan

### Database Migrations

None required - Project entity and schema already exist.

### Backward Compatibility

- [x] Existing pages unaffected
- [x] Tickets without project assignment will need migration (handled by import.sql default project)

### Deployment Steps

1. Deploy updated application
2. Verify new project pages accessible
3. Verify ticket creation with project selection

### Rollback Plan

Remove new controllers, views, and DTO. Revert TicketWeb changes.

---

## Dependencies

**External Libraries:**
- None (uses existing Bootstrap, Thymeleaf)

**Internal Components:**
- `ProjectService` (existing)
- `ModelMapper` (existing)
- Spring Security (existing session auth)

---

## Documentation Updates

- [ ] Update `docs/architecture.md`
- [x] Add inline code documentation (Javadoc on controllers)
- [ ] Update `agent.md` if needed
- [ ] Update `CLAUDE.md` if needed

---

## Open Questions

None - design follows established patterns.

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-02-01 | Use dropdown for project selection | Consistent with existing form patterns |
| 2025-02-01 | Require project on ticket creation only | Existing tickets have default project |
| 2025-02-01 | All users can manage projects | Simplicity; admin-only can be added later |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2025-02-01 | Draft | Initial design document created |
