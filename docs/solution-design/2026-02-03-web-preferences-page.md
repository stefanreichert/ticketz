# Web Preferences Page

**Date**: 2026-02-03
**Author**: Claude
**Status**: Draft

---

## Problem Statement

Users can update their name and password through the `UserService` access port, but there is no web UI to expose these operations. Authenticated users have no way to manage their profile through the web interface.

## Goals

- Provide a web page where authenticated users can update their firstname and lastname
- Provide a web page where authenticated users can change their password (with current password verification)
- Keep name editing and password changing as independent operations on the same page
- Follow existing web adapter patterns (controller, DTO, template, navigation)

## Non-Goals

- REST API endpoints for profile management (separate concern)
- Admin-initiated profile changes
- Email/account deletion
- Profile picture or avatar

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- Which adapters: [x] Web [ ] REST [ ] Persistence
- Which services: [ ] TicketService [x] UserService [ ] CommentService [ ] ProjectService
- New components: [x] Yes (controller, DTOs, template)

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core
- [x] New ports/adapters properly separated
- [x] No framework dependencies in domain layer

### Design Details

**URL Structure:**
```
GET  /secure/preferences/{email}          — show preferences page
POST /secure/preferences/{email}/name     — update name
POST /secure/preferences/{email}/password — change password
```

Uses the user's business identifier (email) as the path variable, following the same pattern as tickets (`/secure/tickets/{ticketNumber}`) and projects (`/secure/projects/{code}`).

**PreferencesWeb DTO** (`adapter/web/model/PreferencesWeb.java`):
```java
@Data
public class PreferencesWeb {
    @NotBlank @Size(max = 255)
    private String firstname;
    @NotBlank @Size(max = 255)
    private String lastname;
    @Email @NotBlank @Size(max = 255)
    private String email;
}
```

**PasswordChangeWeb DTO** (`adapter/web/model/PasswordChangeWeb.java`):
```java
@Data
public class PasswordChangeWeb {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
```

Two separate DTOs keep the name and password forms independent — validation errors on one form do not affect the other.

**PreferencesController** (`adapter/web/controller/PreferencesController.java`):
- `GET /secure/preferences/{email}` — load user by email via `UserService.findByEmail()`, map to `PreferencesWeb`, add both DTOs to model
- `POST /secure/preferences/{email}/name` — validate `PreferencesWeb`, map to domain `User`, call `userService.updateName()`, redirect back to preferences
- `POST /secure/preferences/{email}/password` — validate `PasswordChangeWeb`, check `newPassword == confirmPassword`, call `userService.updatePassword(email, currentPassword, newPassword)`, redirect back to preferences

**Action Constants** (`adapter/web/Action.java`):
```java
public static final String ACTION_SHOW_PREFERENCES = "/secure/preferences/{email}";
public static final String ACTION_SAVE_PREFERENCES_NAME = "/secure/preferences/{email}/name";
public static final String ACTION_SAVE_PREFERENCES_PASSWORD = "/secure/preferences/{email}/password";
```

**View Constant** (`adapter/web/View.java`):
```java
public static final String VIEW_PREFERENCES = "/preferences";
```

**Template** (`templates/preferences.html`):
Single page with two card sections, each containing an independent form:
1. **Name section** — firstname, lastname fields (email displayed read-only), save button
2. **Password section** — current password, new password, confirm password, change button

**Navigation** (`templates/fragments/header.html`):
Add "Preferences" link next to the user display, using `activeNav='preferences'`.

### Implementation Approach

1. **Phase 1**: Constants and DTOs
   - Add URL constants to `Action.java`
   - Add view constant to `View.java`
   - Create `PreferencesWeb.java`
   - Create `PasswordChangeWeb.java`

2. **Phase 2**: Controller
   - Create `PreferencesController.java` with GET (show), POST name, POST password endpoints
   - Use `@Qualifier("webModelMapper")` for domain-DTO mapping

3. **Phase 3**: Template and navigation
   - Create `preferences.html` with two independent forms
   - Add navigation link in `header.html`
   - Add i18n messages to `messages.properties`

No domain model, service, or persistence changes required — `UserService.updateName()` and `UserService.updatePassword()` are already implemented.

---

## Alternatives Considered

### Alternative 1: Separate pages for name and password

**Description**: Two separate pages — one for name editing, one for password changing.

**Pros:**
- Simpler individual pages
- Cleaner URL routing

**Cons:**
- More navigation overhead for users
- More templates and controller methods to maintain
- Users expect a single preferences/settings page

**Decision**: Rejected. A single page with two independent forms is the standard UX pattern and reduces navigation friction.

### Alternative 2: Password fields as @RequestParam instead of DTO

**Description**: Use `@RequestParam` for password fields instead of a dedicated `PasswordChangeWeb` DTO.

**Pros:**
- One fewer class to create

**Cons:**
- No bean validation on password fields
- Inconsistent with the DTO pattern used throughout the web adapter
- Cannot use `@Valid` for automatic validation

**Decision**: Rejected. A dedicated DTO provides consistent validation and follows existing patterns.

---

## Testing Strategy

### Unit Tests
- Not required for web controllers (following project convention — no existing web controller unit tests)

### Integration Tests
- Existing `UserService` tests cover `updateName()` and `updatePassword()` business logic

### Manual Testing
1. Log in, navigate to Preferences via header link
2. Change firstname and lastname, save — verify changes persist after page reload
3. Change password with correct current password — verify login works with new password
4. Change password with wrong current password — verify error message displayed
5. Submit password change with mismatched new/confirm passwords — verify error message

---

## Security Considerations

**Authentication:**
- [x] Web session handling — all URLs under `/secure/` require authentication

**Authorization:**
- [x] `@PreAuthorize("hasRole('ROLE_USER')")` on service methods (already in place)
- [x] Controller endpoints under `/secure/` path (session-authenticated)

**Data Protection:**
- [x] Current password verified before allowing change (service layer)
- [x] New password encoded with BCrypt before storage (service layer)
- [x] Input validation via `@Valid` on DTOs
- [x] Email displayed read-only (not editable)

**CSRF:**
- [x] Web forms protected (Spring Security CSRF enabled by default for web adapter)

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
- `UserService.updateName()` — already implemented
- `UserService.updatePassword()` — already implemented
- `UserService.findByEmail()` — already implemented
- `webModelMapper` bean — already configured

---

## Open Questions

None.

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2026-02-03 | Draft | Initial creation |
