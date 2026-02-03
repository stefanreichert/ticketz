# Editable User Firstname and Lastname

**Date**: 2026-02-03
**Author**: Claude
**Status**: Draft

---

## Problem Statement

Users cannot update their firstname and lastname after account creation. The `UserPersistence` port already provides an `update` method, but the `UserService` access port does not expose an update operation, leaving no path for adapters to trigger a name change.

## Goals

- Allow authenticated users to update their own firstname and lastname
- Expose the update operation through the `UserService` access port
- Maintain hexagonal architecture compliance

## Non-Goals

- Changing email (business identifier, immutable)
- Changing password (separate concern)
- Changing roles (admin operation)
- Adding web or REST adapter endpoints (separate follow-up)

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- Which adapters: [ ] Web [ ] REST [x] Persistence
- Which services: [ ] TicketService [x] UserService [ ] CommentService
- New components: [ ] Yes [x] No

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core
- [x] New ports/adapters properly separated
- [x] No framework dependencies in domain layer

### Design Details

**Service Port Change** (`UserService.java`):
```java
@PreAuthorize("hasRole('ROLE_USER')")
User updateName(@Valid User user);
```

**Service Implementation** (`UserServiceImpl.java`):
- Load existing user by email to verify existence
- Update only firstname and lastname from the provided user
- Delegate to `userPersistence.update()`

**Validation:**
- Bean validation via `@Valid` on the `User` parameter ensures `@NotNull` and `@Size(max=255)` constraints on firstname/lastname are enforced
- The service must ensure only name fields are modified (not roles, passwordHash, etc.)

### Implementation Approach

1. **Phase 1**: Service layer
   - Add `updateName(User user)` to `UserService` port interface with `@PreAuthorize("hasRole('ROLE_USER')")`
   - Implement in `UserServiceImpl`: load existing user, copy only firstname/lastname, delegate to `userPersistence.update()`

No persistence or entity changes required — `UserPersistence.update()` and `UserJPAPersistenceImpl.update()` already support full user updates.

---

## Alternatives Considered

### Alternative 1: Generic `update(User)` method

**Description**: Expose a generic `update` that passes the full User object through to persistence.

**Pros:**
- Simpler interface, reusable for future fields

**Cons:**
- Allows callers to accidentally modify roles, passwordHash, or email
- Violates principle of least privilege

**Decision**: Rejected. A targeted `updateName` method is safer and communicates intent clearly.

---

## Testing Strategy

### Unit Tests
- `UserServiceImplTest.testUpdateName_validUser`: verify firstname/lastname updated, other fields unchanged
- `UserServiceImplTest.testUpdateName_unknownEmail`: verify exception when user does not exist

### Integration Tests
- Existing `UserJPAPersistenceImpl` tests already cover the `update` path

### Manual Testing
1. Call `updateName` via a future web/REST endpoint and verify the name change persists
2. Verify that roles and passwordHash remain unchanged after update

---

## Security Considerations

**Authorization:**
- [x] `@PreAuthorize("hasRole('ROLE_USER')")` on the new method
- Service enforces that only firstname/lastname are modified

**Data Protection:**
- [x] Input validation via `@Valid` on User parameter
- [x] Immutable fields (email, roles, passwordHash) protected by service logic

---

## Migration Plan

### Database Migrations
No schema changes required. The `UserEntity` already stores firstname and lastname as updatable columns.

### Backward Compatibility
- [x] API version unchanged
- [x] Existing endpoints unaffected

---

## Dependencies

**Internal Components:**
- `UserPersistence.update()` — already implemented
- `UserJPAPersistenceImpl.update()` — already implemented

---

## Open Questions

None.

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2026-02-03 | Draft | Initial creation |
