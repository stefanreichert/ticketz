# [Feature/Change Name]

**Date**: YYYY-MM-DD  
**Author**: [Your Name]  
**Status**: Draft

---

## Problem Statement

Describe the problem or requirement that needs to be addressed. What is the current limitation or issue?

## Goals

- Goal 1: Clear, measurable objective
- Goal 2: Another objective
- Goal 3: Success criteria

## Non-Goals

- What this solution will NOT address
- Out-of-scope items
- Future considerations

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- Which adapters: [ ] Web [ ] REST [ ] Persistence
- Which services: [ ] TicketService [ ] UserService [ ] CommentService
- New components: [ ] Yes [ ] No

**Hexagonal Architecture Compliance:**
- [ ] Business logic remains in core
- [ ] New ports/adapters properly separated
- [ ] No framework dependencies in domain layer

### Design Details

**Domain Model Changes:**
```java
// Example: New or modified domain classes
public class Example {
    // Key fields and methods
}
```

**API Changes:**
```
POST /api/resource
GET /api/resource/{id}
```

**Database Schema:**
- New tables/columns
- Migrations needed
- Indexes required

### Implementation Approach

1. **Phase 1**: [Description]
   - Task 1.1
   - Task 1.2

2. **Phase 2**: [Description]
   - Task 2.1
   - Task 2.2

3. **Phase 3**: [Description]
   - Task 3.1
   - Task 3.2

**Estimated Effort**: [X days/weeks]

---

## Alternatives Considered

### Alternative 1: [Name]

**Description**: Brief explanation of the alternative approach

**Pros:**
- Advantage 1
- Advantage 2

**Cons:**
- Disadvantage 1
- Disadvantage 2

**Decision**: Why this was rejected

### Alternative 2: [Name]

[Same structure as Alternative 1]

---

## Testing Strategy

### Unit Tests
- Test service layer business logic
- Mock dependencies
- Coverage target: 80%+

### Integration Tests
- Test adapter layer with Spring context
- Test database interactions
- Test REST endpoints with MockMvc

### Manual Testing
1. Scenario 1: [Description]
2. Scenario 2: [Description]
3. Edge cases: [Description]

---

## Security Considerations

**Authentication:**
- [ ] Web session handling
- [ ] JWT token requirements
- [ ] No authentication changes

**Authorization:**
- [ ] New roles required
- [ ] Existing role changes
- [ ] Permission checks needed at: [locations]

**Data Protection:**
- [ ] Sensitive data handling
- [ ] Input validation requirements
- [ ] Output sanitization

**CSRF:**
- [ ] Web forms protected
- [ ] API endpoints stateless

---

## Migration Plan

### Database Migrations
```sql
-- Example migration script
ALTER TABLE ticket ADD COLUMN new_field VARCHAR(255);
```

### Backward Compatibility
- [ ] API version unchanged
- [ ] Existing endpoints unaffected
- [ ] Deprecated features marked

### Deployment Steps
1. Database migration
2. Deploy new code
3. Verify functionality
4. Update documentation

### Rollback Plan
- Rollback procedure if issues occur
- Data migration reversal steps

---

## Dependencies

**External Libraries:**
- Library name (version) - purpose

**Internal Components:**
- Component dependencies
- Service interactions

---

## Documentation Updates

- [ ] Update `docs/architecture.md`
- [ ] Update API documentation
- [ ] Update `agent.md` if needed
- [ ] Add inline code documentation

---

## Open Questions

1. **Question 1?**
   - Context and implications
   - Possible answers

2. **Question 2?**
   - Context and implications
   - Possible answers

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| YYYY-MM-DD | Decision description | Why it was made |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| YYYY-MM-DD | Draft | Initial creation |
| YYYY-MM-DD | Under Review | Sent for team review |
| YYYY-MM-DD | Approved | Ready for implementation |
| YYYY-MM-DD | Implemented | Feature completed |
