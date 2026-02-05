# Ticket List Filter

**Date**: 2026-02-05
**Author**: Claude
**Status**: Implemented

---

## Problem Statement

The ticket list page displays all tickets without any filtering capability. As the number of tickets grows, users need a way to quickly find specific tickets. The filter should search across multiple fields (title, description, ticket number, author name, editor name, state, project) but provide a simple, single text input in the UI.

## Goals

- Enable users to filter tickets using a single search text field
- Search across: ticket number, title, description, author name (first + last), editor name (first + last), state, and project code/name
- Case-insensitive search with partial matching
- Maintain hexagonal architecture compliance
- Support both Web UI and REST API

## Non-Goals

- Advanced search syntax (AND/OR operators, field-specific prefixes)
- Pagination (can be added later)
- Saved filters or filter history
- Full-text search indexing (keep it simple with LIKE queries)

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- [x] Web adapter
- [x] REST adapter
- [x] Persistence adapter
- [x] TicketService

**New Components:**
- [x] `TicketSearchCriteria` domain model
- [x] Repository query method

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core (search criteria defined in domain)
- [x] New ports/adapters properly separated
- [x] No framework dependencies in domain layer

### Design Details

#### Layer 1: Persistence

**TicketRepository** — Add custom query method using JPQL with OR conditions across all searchable fields.

**Important:** The `editor` relationship is optional (`@ManyToOne` without `optional = false`), so we must use an explicit `LEFT JOIN` to avoid excluding tickets with null editors. Implicit joins via path expressions (e.g., `t.editor.firstname`) would create an inner join and filter out null-editor tickets.

```java
@Query("""
    SELECT t FROM TicketEntity t
    LEFT JOIN t.editor e
    WHERE LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(t.author.firstname) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(t.author.lastname) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(e.firstname) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(e.lastname) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(CAST(t.state AS string)) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(t.project.code) LIKE LOWER(CONCAT('%', :searchText, '%'))
       OR LOWER(t.project.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
    """)
List<TicketEntity> search(@Param("searchText") String searchText);
```

Note: `author` and `project` are `@ManyToOne(optional = false)`, so implicit inner joins are safe for those.

**TicketPersistence** — Add port method:

```java
/**
 * Search tickets by text across multiple fields.
 * Searches: ticketNumber, title, description, author name, editor name, state, project code/name.
 *
 * @param searchText the text to search for (case-insensitive, partial match)
 * @return list of matching tickets
 */
List<Ticket> search(@NotBlank String searchText);
```

**TicketJPAPersistenceImpl** — Implement the search method:

```java
@Override
public List<Ticket> search(String searchText) {
    return ticketRepository.search(searchText).stream()
            .map(ticketEntity -> mapper.map(ticketEntity, Ticket.class))
            .toList();
}
```

#### Layer 2: Business Logic (Service)

**TicketService** — Add access port method:

```java
/**
 * Search tickets by text across multiple fields.
 * Returns all tickets if searchText is null or blank.
 *
 * @param searchText the text to search for (case-insensitive, partial match)
 * @return list of matching tickets with possible next states populated
 */
List<Ticket> search(String searchText);
```

**TicketServiceImpl** — Implement with fallback to findAll for empty search:

```java
@Override
public List<Ticket> search(String searchText) {
    List<Ticket> tickets;
    if (searchText == null || searchText.isBlank()) {
        tickets = ticketPersistence.findAll();
    } else {
        tickets = ticketPersistence.search(searchText.trim());
    }
    tickets.forEach(this::updatePossibleNextStates);
    return tickets;
}
```

#### Layer 3: REST API

**RestTicketController** — Add optional query parameter to existing endpoint:

```java
@GetMapping
@PreAuthorize("hasRole('ROLE_API')")
public ResponseEntity<List<TicketRest>> allTickets(
        @RequestParam(required = false) String search) {
    List<TicketRest> ticketRests = ticketService.search(search)
            .stream()
            .map(ticket -> mapper.map(ticket, TicketRest.class))
            .toList();
    return ResponseEntity.ok(ticketRests);
}
```

**API Usage:**
```
GET /api/tickets              → all tickets
GET /api/tickets?search=bug   → tickets matching "bug"
```

#### Layer 4: Web UI

**TicketListController** — Change from @ModelAttribute to explicit method with search parameter:

```java
@GetMapping(value = ACTION_SHOW_TICKET_LIST)
public String showTicketList(@RequestParam(required = false) String search, Model model) {
    List<TicketWeb> tickets = ticketService.search(search).stream()
            .map(ticket -> mapper.map(ticket, TicketWeb.class))
            .toList();
    model.addAttribute(ATTRIBUTE_NAME_TICKETS, tickets);
    model.addAttribute(ATTRIBUTE_NAME_SEARCH, search);
    return VIEW_TICKET_LIST;
}
```

**ticket_list.html** — Add search form above the table:

```html
<div class="mb-3">
    <form th:action="@{/secure/tickets}" method="get" class="row g-2 align-items-center">
        <div class="col-auto">
            <input type="text" name="search" class="form-control"
                   th:value="${search}" th:placeholder="#{label.search.placeholder}"/>
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-primary" th:text="#{button.search}"/>
        </div>
        <div class="col-auto" th:if="${search != null and !search.isEmpty()}">
            <a th:href="@{/secure/tickets}" class="btn btn-outline-secondary" th:text="#{button.clear}"/>
        </div>
    </form>
</div>
```

**messages.properties** — Add new labels:

```properties
label.search.placeholder=Search tickets...
button.search=Search
button.clear=Clear
```

### Implementation Phases

1. **Phase 1: Persistence Layer**
   - Add `search` method to `TicketRepository` with JPQL query
   - Add `search` method to `TicketPersistence` interface
   - Implement in `TicketJPAPersistenceImpl`

2. **Phase 2: Service Layer**
   - Add `search` method to `TicketService` interface
   - Implement in `TicketServiceImpl` with empty-string fallback

3. **Phase 3: REST Adapter**
   - Add `search` query parameter to `RestTicketController.allTickets()`
   - Update existing REST integration tests

4. **Phase 4: Web Adapter**
   - Refactor `TicketListController` to use search parameter
   - Add search form to `ticket_list.html`
   - Add i18n messages

5. **Phase 5: Testing**
   - Add JPA integration test for search query
   - Add REST integration test for search endpoint
   - Manual testing of web UI

---

## Alternatives Considered

### Alternative 1: Specification Pattern (JPA Criteria API)

**Description**: Use Spring Data JPA Specifications for dynamic query building.

**Pros:**
- Type-safe query construction
- Easier to add new filter fields
- Composable filters

**Cons:**
- More complex implementation
- Overkill for single text field search
- Harder to read/maintain for this simple use case

**Decision**: Rejected — JPQL query is simpler and sufficient for current requirements.

### Alternative 2: Client-Side Filtering

**Description**: Load all tickets and filter in JavaScript.

**Pros:**
- No backend changes needed
- Instant filtering without server round-trip

**Cons:**
- Doesn't scale with large ticket counts
- Inconsistent with REST API
- Duplicates filter logic

**Decision**: Rejected — Server-side filtering is more scalable and consistent.

### Alternative 3: Separate Search Endpoint

**Description**: Create `/api/tickets/search` instead of query parameter.

**Pros:**
- Clear separation of concerns
- Could support POST for complex search criteria

**Cons:**
- Inconsistent REST semantics (search is a read operation)
- More endpoints to maintain

**Decision**: Rejected — Query parameter on GET is REST-idiomatic for filtering.

---

## Testing Strategy

### Integration Tests

**JPA Layer:**
```java
@Test
void testSearch_matchesTitle() {
    // Given: ticket with title "Login Bug"
    // When: search("login")
    // Then: returns the ticket
}

@Test
void testSearch_matchesAuthorName() {
    // Given: ticket with author "John Doe"
    // When: search("doe")
    // Then: returns the ticket
}

@Test
void testSearch_noMatch() {
    // Given: existing tickets
    // When: search("nonexistent")
    // Then: returns empty list
}
```

**REST Layer:**
```java
@Test
void testAllTickets_withSearchParam() {
    // GET /api/tickets?search=bug
    // Verify filtered results
}
```

### Manual Testing
1. Search by ticket number (partial match)
2. Search by title keyword
3. Search by author first/last name
4. Search by state (e.g., "progress")
5. Search by project code
6. Clear search returns all tickets
7. Empty search field shows all tickets

---

## Security Considerations

**Authentication:**
- [x] Web session handling (existing)
- [x] JWT token requirements (existing)
- [x] No authentication changes

**Authorization:**
- [x] Existing role checks apply
- [x] No new permissions needed

**Data Protection:**
- [x] Input validation: search text trimmed, used in parameterized query (SQL injection safe)
- [x] No sensitive data exposed

**CSRF:**
- [x] Search form uses GET (no CSRF token needed for read operations)

---

## Migration Plan

### Database Migrations
- None required (no schema changes)

### Backward Compatibility
- [x] API version unchanged
- [x] Existing endpoints unaffected (search parameter is optional)
- [x] No deprecated features

### Deployment Steps
1. Deploy new code
2. Verify search functionality

### Rollback Plan
- Revert code deployment (no database changes to revert)

---

## Dependencies

**External Libraries:**
- None (uses existing Spring Data JPA)

**Internal Components:**
- `TicketPersistence` port
- `TicketService` port
- `TicketRepository`

---

## Open Questions

None.

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-02-05 | Use JPQL OR query | Simple, readable, sufficient for single text search |
| 2026-02-05 | Optional query parameter on existing endpoint | REST-idiomatic, backward compatible |
| 2026-02-05 | Fallback to findAll for empty search | Consistent UX, no special "show all" button needed |
| 2026-02-05 | Case-insensitive search for all fields including state | Better UX, users don't need to know exact case |
| 2026-02-05 | Use LEFT JOIN for editor relationship | Editor is optional; implicit join would exclude null-editor tickets |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2026-02-05 | Draft | Initial creation |
| 2026-02-05 | Implemented | All layers implemented, tests passing |
