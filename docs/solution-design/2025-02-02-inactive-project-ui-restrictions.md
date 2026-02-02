# Inactive Project UI Restrictions

**Date**: 2025-02-02
**Author**: Claude
**Status**: Implemented

---

## Problem Statement

The backend already rejects ticket operations (create, update, delete) for projects that are inactive. However, the UI does not reflect this restriction:

1. Ticket detail page shows edit controls even when the ticket belongs to an inactive project
2. Ticket list shows edit/delete buttons for tickets in inactive projects
3. Users can attempt actions that will fail, resulting in confusing error messages

## Goals

- Goal 1: Visually indicate when a ticket belongs to an inactive project
- Goal 2: Disable edit/save/delete controls for tickets in inactive projects
- Goal 3: Provide clear messaging explaining why actions are disabled
- Goal 4: Maintain consistency with existing UI patterns

## Non-Goals

- Changing backend validation logic (already implemented correctly)
- Adding ability to reactivate projects from ticket views
- Bulk operations on tickets when project becomes inactive
- REST API changes (already returns appropriate errors)

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- Which adapters: [x] Web [ ] REST [ ] Persistence
- Which services: [ ] TicketService [ ] UserService [ ] CommentService [ ] ProjectService
- New components: [ ] Yes [x] No

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core (no changes needed)
- [x] New ports/adapters properly separated
- [x] No framework dependencies in domain layer

### Design Details

**Domain Model Changes:**

No domain model changes required. The `Project.isActive()` field already exists.

**Web Layer Changes:**

1. **TicketWeb DTO** - Replace separate project fields with ProjectWeb reference:
```java
public class TicketWeb {
    // ... existing fields ...

    // REMOVE these fields:
    // private String projectCode;
    // private String projectName;

    // ADD single ProjectWeb reference:
    @NotNull
    private ProjectWeb project;
}
```

This consolidates `projectCode`, `projectName`, and provides access to `active` status via `project.isActive()`.

2. **TicketController** - Update to use ProjectWeb:
```java
// In showTicket():
ProjectWeb projectWeb = mapper.map(existingTicket.getProject(), ProjectWeb.class);
ticket.setProject(projectWeb);

// In newTicket(): set project to null or empty ProjectWeb for new tickets

// In saveTicket(): get project code from ticket.getProject().getCode()

// Modify canEdit logic to consider project status:
ticket.setCanEdit(ticketService.evaluateCanBeEdited(existingTicket)
    && existingTicket.getProject().isActive());
```

3. **ticket.html** - Update project references and add inactive warning:
```html
<!-- Warning banner when project is inactive -->
<div th:if="${ticketWeb.project != null and !ticketWeb.project.active}" class="alert alert-warning">
    <span th:text="#{message.ticket.project_inactive}"></span>
</div>

<!-- Project display for existing tickets -->
<span th:text="${ticketWeb.project.code + ' - ' + ticketWeb.project.name}"></span>

<!-- Project dropdown for new tickets (unchanged - still uses active projects list) -->

<!-- Hidden field update -->
<input type="hidden" name="project.code" th:field="*{project.code}"/>
```

4. **ticket_list.html** - Visual indication and disabled buttons:
```html
<!-- Show inactive badge next to project code -->
<td>
    <span th:text="${ticket.project.code}"></span>
    <span th:unless="${ticket.project.active}" class="badge bg-secondary ms-1">Inactive</span>
</td>

<!-- Disable edit/delete buttons for inactive projects -->
<a th:classappend="${!ticket.project.active ? 'disabled' : ''}" ...>Edit</a>
<button th:disabled="${!ticket.project.active}" ...>Delete</button>
```

**i18n Messages:**
```properties
message.ticket.project_inactive=This ticket belongs to an inactive project and cannot be modified.
```

### Implementation Approach

1. **Phase 1**: Refactor TicketWeb to use ProjectWeb
   - Remove `projectCode` and `projectName` fields from TicketWeb
   - Add `ProjectWeb project` field to TicketWeb
   - Update TicketController.showTicket() to map and set ProjectWeb
   - Update TicketController.newTicket() to handle project for new tickets
   - Update TicketController.saveTicket() to read project code from ProjectWeb
   - Update canEdit logic to consider project active status

2. **Phase 2**: Update ticket detail page (ticket.html)
   - Update all `ticketWeb.projectCode`/`projectName` references to `ticketWeb.project.code`/`name`
   - Add warning banner for inactive projects
   - Ensure all action buttons respect the inactive state
   - State transition buttons should also be hidden/disabled
   - Update hidden field for project

3. **Phase 3**: Update ticket list page (ticket_list.html)
   - Add visual indicator (badge) for tickets in inactive projects
   - Disable edit/delete buttons for those tickets

4. **Phase 4**: Add i18n message and test

---

## Alternatives Considered

### Alternative 1: Filter out tickets from inactive projects

**Description**: Hide tickets from inactive projects entirely in the list view

**Pros:**
- Simpler UI with fewer edge cases
- Users only see actionable tickets

**Cons:**
- Users lose visibility of historical tickets
- May cause confusion about "missing" tickets
- Breaks ticket number continuity in views

**Decision**: Rejected - users need to see all tickets for historical reference

### Alternative 2: Server-side only validation (current state)

**Description**: Keep the current approach where backend validates and returns errors

**Pros:**
- No UI changes needed
- Backend is already secure

**Cons:**
- Poor user experience (try action → see error)
- Confusing error messages
- Users don't know why actions fail before trying

**Decision**: Rejected - does not meet UX goals

---

## Testing Strategy

### Unit Tests
- No new service-layer tests needed (backend unchanged)

### Integration Tests
- Test TicketController correctly maps ProjectWeb
- Test canEdit is false when project is inactive

### Manual Testing
1. View ticket in active project → all controls enabled
2. View ticket in inactive project → warning shown, controls disabled
3. Ticket list shows inactive badge for relevant tickets
4. Edit/delete buttons disabled in list for inactive project tickets

---

## Security Considerations

**Authentication:**
- [x] No authentication changes

**Authorization:**
- [x] No authorization changes (backend already enforces)

**Data Protection:**
- [x] No sensitive data handling changes

**CSRF:**
- [x] No CSRF changes needed

---

## Migration Plan

### Database Migrations
None required.

### Backward Compatibility
- [x] No API changes
- [x] Existing endpoints unaffected
- [x] Pure UI enhancement

### Deployment Steps
1. Deploy updated code
2. Verify functionality
3. No migration needed

### Rollback Plan
- Revert to previous code version if issues occur
- No data changes to reverse

---

## Dependencies

**External Libraries:**
- None new

**Internal Components:**
- TicketWeb
- TicketController
- ticket.html
- ticket_list.html
- messages.properties

---

## Documentation Updates

- [ ] Update `agent.md` if needed
- [ ] Add inline code documentation

---

## Open Questions

None.

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2025-02-02 | Use badge next to project code for inactive indicator | Clear, consistent with project list styling |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2025-02-02 | Draft | Initial creation |
| 2025-02-02 | Implemented | Feature completed |
