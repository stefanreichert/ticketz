# Editable User Password

**Date**: 2026-02-03
**Author**: Claude
**Status**: Draft

---

## Problem Statement

Users cannot change their password after account creation. There is no password update operation exposed through the `UserService` access port.

## Goals

- Allow authenticated users to change their own password
- Require verification of the current password before allowing change
- Expose the operation through the `UserService` access port

## Non-Goals

- Password reset / "forgot password" flow (no email verification infrastructure)
- Admin-initiated password reset
- Password strength validation rules (future consideration)
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
User updatePassword(String email, String currentPassword, String newPassword);
```

**Service Implementation** (`UserServiceImpl.java`):
- Load existing user by email
- Verify current password using `passwordEncoder.matches()`
- Throw `ValidationException` if current password is incorrect
- Encode new password using `passwordEncoder.encode()`
- Delegate to `userPersistence.update()`

**Method Signature Rationale:**
Plain string parameters instead of a User object — password change is a targeted operation that does not require the full domain model. The email identifies the user, and passwords are handled as raw strings (never stored unencoded).

### Implementation Approach

1. **Phase 1**: Service layer
   - Add `updatePassword(String email, String currentPassword, String newPassword)` to `UserService` port interface
   - Implement in `UserServiceImpl` with current password verification

No persistence or entity changes required — `UserPersistence.update()` already supports updating the passwordHash field.

---

## Alternatives Considered

### Alternative 1: Accept new password without verification

**Description**: Skip current password check, just set the new password.

**Pros:**
- Simpler implementation

**Cons:**
- Security risk: stolen session could change password without knowing current one
- Violates standard security practices

**Decision**: Rejected. Current password verification is a security requirement.

---

## Testing Strategy

### Unit Tests
- `testUpdatePassword_validCurrentPassword`: verify password is updated when current password matches
- `testUpdatePassword_invalidCurrentPassword`: verify `ValidationException` thrown when current password is wrong
- `testUpdatePassword_unknownEmail`: verify exception when user does not exist

### Integration Tests
- Existing `UserJPAPersistenceImpl` tests already cover the `update` path

### Manual Testing
1. Call `updatePassword` with correct current password — verify login works with new password
2. Call `updatePassword` with wrong current password — verify `ValidationException`

---

## Security Considerations

**Authorization:**
- [x] `@PreAuthorize("hasRole('ROLE_USER')")` on the new method

**Data Protection:**
- [x] Current password verified before allowing change
- [x] New password encoded with BCrypt (cost factor 12) before storage
- [x] Plain text passwords never persisted

---

## Migration Plan

### Database Migrations
No schema changes required.

### Backward Compatibility
- [x] API version unchanged
- [x] Existing endpoints unaffected

---

## Dependencies

**Internal Components:**
- `UserPersistence.update()` — already implemented
- `PasswordEncoder` — already injected into `UserServiceImpl`
- `ValidationException` — already exists in service layer

---

## Open Questions

None.

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2026-02-03 | Draft | Initial creation |
