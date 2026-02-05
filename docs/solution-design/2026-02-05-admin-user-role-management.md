# Admin User Role Management

**Date**: 2026-02-05
**Author**: Claude
**Status**: Draft

---

## Problem Statement

Admin users need a way to manage roles for existing users. Currently, roles are only assigned during user creation (signup). There is no UI or service method to modify user roles after creation.

## Goals

- Allow admin users (ROLE_ADMIN) to view all users and their current roles
- Allow admin users to modify roles for any user
- Prevent admins from removing their own ROLE_ADMIN (safety measure)
- Provide a simple, intuitive web interface

## Non-Goals

- User creation/deletion via admin UI (signup handles creation)
- Password reset by admin
- REST API for role management (can be added later)
- Role hierarchy or permissions beyond the existing roles

---

## Proposed Solution

### Architecture Impact

**Affected Components:**
- [x] Web adapter (new controllers and templates)
- [ ] REST adapter (not in this iteration)
- [ ] Persistence adapter (existing `update` method sufficient)
- [x] UserService (new `updateRoles` method)

**New Components:**
- [x] `UserListController` - list all users
- [x] `UserController` - view/edit single user roles
- [x] `UserWeb` model - web layer user representation
- [x] `user_list.html` and `user.html` templates

**Hexagonal Architecture Compliance:**
- [x] Business logic remains in core (role update validation in service)
- [x] New ports/adapters properly separated
- [x] No framework dependencies in domain layer

### Design Details

#### Layer 1: Persistence

The existing `UserPersistence.update()` method already handles updating user entities including roles. No changes required.

```java
// Already exists
User update(@Valid User user);
```

#### Layer 2: Business Logic (Service)

**UserService** — Add new method for role updates:

```java
/**
 * Update the roles of a user. Only accessible by admins.
 * Validates that the admin cannot remove their own ROLE_ADMIN.
 *
 * @param email the user's email address
 * @param roles the new set of roles
 * @return the updated user
 */
@PreAuthorize("hasRole('ROLE_ADMIN')")
User updateRoles(@Email @NotNull @Size(max = 255) String email, @NotNull Set<Role> roles);
```

**UserServiceImpl** — Implementation with self-demotion protection:

```java
@Override
@PreAuthorize("hasRole('ROLE_ADMIN')")
public User updateRoles(String email, Set<Role> roles) {
    User currentUser = getCurrentUser();
    User targetUser = userPersistence.loadByEmail(email);

    // Prevent admin from removing their own admin role
    if (currentUser.getEmail().equals(email) && !roles.contains(Role.ROLE_ADMIN)) {
        throw new ValidationException("Cannot remove your own admin role");
    }

    targetUser.setRoles(roles);
    return userPersistence.update(targetUser);
}
```

#### Layer 3: Web Adapter

**New Action Constants:**

```java
public static final String ACTION_SHOW_USER_LIST = "/secure/users";
public static final String ACTION_SHOW_USER = "/secure/users/{email}";
public static final String ACTION_SAVE_USER_ROLES = "/secure/users/{email}/roles";
```

**New View Constant:**

```java
public static final String VIEW_USER_LIST = "/user_list";
public static final String VIEW_USER = "/user";
```

**UserWeb Model:**

```java
@Data
public class UserWeb {
    private String email;
    private String firstname;
    private String lastname;
    private Set<String> roles = new HashSet<>();
    private Long version;
}
```

**UserListController:**

```java
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserListController {

    private static final String ATTRIBUTE_NAME_USERS = "users";

    private final UserService userService;
    private final ModelMapper mapper;

    @GetMapping(ACTION_SHOW_USER_LIST)
    public String showUserList(Model model) {
        List<UserWeb> users = userService.findAll().stream()
                .map(user -> mapper.map(user, UserWeb.class))
                .toList();
        model.addAttribute(ATTRIBUTE_NAME_USERS, users);
        return VIEW_USER_LIST;
    }
}
```

**UserController:**

```java
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private static final String ATTRIBUTE_NAME_USER = "user";
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME_ERROR = "error";

    private final UserService userService;
    private final ModelMapper mapper;
    private final MessageSource messageSource;

    @GetMapping(ACTION_SHOW_USER)
    public String showUser(@PathVariable String email, Model model) {
        User user = userService.findByEmail(email).orElseThrow();
        UserWeb userWeb = mapper.map(user, UserWeb.class);
        model.addAttribute(ATTRIBUTE_NAME_USER, userWeb);
        model.addAttribute("allRoles", List.of(Role.ROLE_USER, Role.ROLE_ADMIN, Role.ROLE_API));
        return VIEW_USER;
    }

    @PostMapping(ACTION_SAVE_USER_ROLES)
    public String saveRoles(@PathVariable String email,
                            @RequestParam(required = false, defaultValue = "") List<String> roles,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        // Validate role strings
        Set<String> validRoleNames = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());
        List<String> invalidRoles = roles.stream()
                .filter(r -> !r.isBlank())
                .filter(r -> !validRoleNames.contains(r))
                .toList();

        if (!invalidRoles.isEmpty()) {
            User user = userService.findByEmail(email).orElseThrow();
            UserWeb userWeb = mapper.map(user, UserWeb.class);
            model.addAttribute(ATTRIBUTE_NAME_USER, userWeb);
            model.addAttribute("allRoles", List.of(Role.ROLE_USER, Role.ROLE_ADMIN, Role.ROLE_API));
            model.addAttribute(ATTRIBUTE_NAME_ERROR, messageSource.getMessage(
                    "message.user.invalid_roles", new String[]{String.join(", ", invalidRoles)}, request.getLocale()));
            return VIEW_USER;
        }

        Set<Role> roleSet = roles.stream()
                .filter(r -> !r.isBlank())
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        userService.updateRoles(email, roleSet);

        String message = messageSource.getMessage("message.user.roles_saved",
                new String[]{email}, request.getLocale());
        redirectAttributes.addFlashAttribute(ATTRIBUTE_NAME_MESSAGE, message);
        return redirectTo(ACTION_SHOW_USER_LIST);
    }
}
```

**user_list.html Template:**

```html
<!-- Admin user list with link to edit roles -->
<table class="table table-striped w-100" style="table-layout: fixed;">
    <thead>
    <tr>
        <th style="width: 25%;" th:text="#{table.header.email}"></th>
        <th style="width: 20%;" th:text="#{table.header.name}"></th>
        <th style="width: 40%;" th:text="#{table.header.roles}"></th>
        <th style="width: 15%;"></th>
    </tr>
    </thead>
    <tbody>
    <tr th:each="user : ${users}">
        <td th:text="${user.email}"></td>
        <td th:text="${user.firstname + ' ' + user.lastname}"></td>
        <td>
            <span th:each="role : ${user.roles}" class="badge bg-secondary me-1" th:text="#{${role}}"></span>
        </td>
        <td class="text-end">
            <a th:href="@{/secure/users/{email}(email=${user.email})}"
               class="btn btn-sm btn-outline-primary" th:text="#{button.edit}"></a>
        </td>
    </tr>
    </tbody>
</table>
```

**user.html Template:**

```html
<!-- User role edit form with checkboxes -->
<form th:action="@{/secure/users/{email}/roles(email=${user.email})}" method="post">
    <div class="card">
        <div class="card-header" th:text="#{label.user.roles_section}"></div>
        <div class="card-body">
            <div class="mb-3">
                <span class="form-label" th:text="#{label.email}"></span>
                <span class="form-control bg-light" th:text="${user.email}"></span>
            </div>
            <div class="mb-3">
                <span class="form-label" th:text="#{label.user.name}"></span>
                <span class="form-control bg-light" th:text="${user.firstname + ' ' + user.lastname}"></span>
            </div>
            <div class="mb-3">
                <label class="form-label" th:text="#{label.user.roles}"></label>
                <div th:each="role : ${allRoles}" class="form-check">
                    <input type="checkbox" class="form-check-input"
                           th:id="${role.name()}" name="roles" th:value="${role.name()}"
                           th:checked="${user.roles.contains(role.name())}"/>
                    <label class="form-check-label" th:for="${role.name()}" th:text="#{${role.name()}}"></label>
                </div>
            </div>
            <div>
                <button type="submit" class="btn btn-primary" th:text="#{button.save}"/>
                <a th:href="@{/secure/users}" class="btn btn-outline-secondary" th:text="#{button.cancel}"/>
            </div>
        </div>
    </div>
</form>
```

**Navigation Update (header.html):**

Add "Users" link visible only to admins:

```html
<li class="nav-item" sec:authorize="hasRole('ROLE_ADMIN')">
    <a class="nav-link" th:classappend="${activeNav == 'users' ? 'text-primary fw-bold' : 'text-secondary'}"
       th:href="@{/secure/users}" th:text="#{button.users}"></a>
</li>
```

**New i18n Messages:**

```properties
# User Management
pageheader.users=Manage user accounts and roles.
pageheader.user=Edit user roles.
button.users=Users
table.header.email=Email
table.header.name=Name
table.header.roles=Roles
label.user.name=Name
label.user.roles=Roles
label.user.roles_section=User Roles
label.user.no_users=No users found.
message.user.roles_saved=Roles for {0} successfully updated.
message.user.invalid_roles=Invalid roles: {0}
message.user.cannot_remove_own_admin=Cannot remove your own admin role.

# Role Labels (like ticket states)
ROLE_USER=User
ROLE_ADMIN=Admin
ROLE_API=API
ROLE_ANONYMOUS=Anonymous
```

### Implementation Phases

1. **Phase 1: Service Layer**
   - Add `updateRoles` method to `UserService` interface
   - Implement in `UserServiceImpl` with self-demotion protection
   - Add unit test for role update validation

2. **Phase 2: Web Model**
   - Create `UserWeb` class in adapter.web.model
   - Add ModelMapper configuration if needed

3. **Phase 3: Web Controllers**
   - Add action constants to `Action.java`
   - Add view constants to `View.java`
   - Create `UserListController`
   - Create `UserController`

4. **Phase 4: Templates**
   - Create `user_list.html`
   - Create `user.html`
   - Update `header.html` with admin-only "Users" link
   - Add i18n messages

5. **Phase 5: Testing**
   - Add integration test for admin role requirements
   - Manual testing of role changes

---

## Alternatives Considered

### Alternative 1: Inline Role Editing in User List

**Description**: Edit roles directly in the user list table with checkboxes.

**Pros:**
- Faster editing, fewer clicks
- All users visible at once

**Cons:**
- Cluttered UI with many checkboxes
- Easy to make accidental changes
- Harder to implement confirmation

**Decision**: Rejected — Separate edit page is cleaner and matches existing patterns (project, ticket).

### Alternative 2: Dropdown/Multi-select for Roles

**Description**: Use a multi-select dropdown instead of checkboxes.

**Pros:**
- More compact UI
- Standard HTML element

**Cons:**
- Less intuitive for small number of options (only 3 roles)
- Harder to see current state at a glance

**Decision**: Rejected — Checkboxes are more intuitive for 3 options.

---

## Testing Strategy

### Unit Tests

**UserServiceImpl:**
```java
@Test
void testUpdateRoles_adminCanUpdateOtherUser() {
    // Given: admin user updating another user's roles
    // When: updateRoles called
    // Then: roles updated successfully
}

@Test
void testUpdateRoles_adminCannotRemoveOwnAdminRole() {
    // Given: admin user trying to remove own admin role
    // When: updateRoles called
    // Then: ValidationException thrown
}

@Test
void testUpdateRoles_nonAdminCannotUpdateRoles() {
    // Given: regular user trying to update roles
    // When: updateRoles called
    // Then: AccessDeniedException thrown
}
```

**UserController (validation):**
```java
@Test
void testSaveRoles_invalidRoleName_returnsErrorMessage() {
    // Given: request with invalid role name "ROLE_INVALID"
    // When: saveRoles called
    // Then: returns user view with error message
}
```

### Manual Testing

1. Login as admin, verify "Users" link visible in nav
2. Login as non-admin, verify "Users" link not visible
3. As admin, view user list
4. Edit roles for a non-admin user
5. Try to remove own admin role (should fail with message)
6. Verify role changes persist after logout/login
7. Submit invalid role value via browser tools (should show error message)

---

## Security Considerations

**Authentication:**
- [x] Web session handling (existing)
- [x] No changes to authentication

**Authorization:**
- [x] All endpoints require ROLE_ADMIN
- [x] Controller-level `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- [x] Service-level `@PreAuthorize` on `updateRoles`
- [x] Navigation link hidden for non-admins

**Data Protection:**
- [x] Self-demotion protection (cannot remove own admin role)
- [x] Version field for optimistic locking

**CSRF:**
- [x] POST form protected by Spring Security CSRF

---

## Migration Plan

### Database Migrations
- None required (no schema changes)

### Backward Compatibility
- [x] No existing functionality affected
- [x] New endpoints only

### Deployment Steps
1. Deploy new code
2. Verify admin users can access /secure/admin/users

### Rollback Plan
- Remove code (no data changes to revert)

---

## Dependencies

**External Libraries:**
- None (uses existing Spring Security, Thymeleaf)

**Internal Components:**
- `UserService` / `UserPersistence`
- Existing web adapter patterns

---

## Open Questions

None.

---

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-02-05 | Separate edit page for roles | Matches existing patterns, cleaner UX |
| 2026-02-05 | Checkboxes for role selection | Intuitive for small number of options |
| 2026-02-05 | Self-demotion protection | Prevents admin from locking themselves out |
| 2026-02-05 | Admin-only navigation link | Clear separation of admin features |
| 2026-02-05 | Routes under /secure/users (not /secure/admin/users) | Simpler URL structure, admin check via @PreAuthorize |
| 2026-02-05 | Roles submitted as string array | More readable controller method than individual booleans |
| 2026-02-05 | i18n labels for roles | Consistent with ticket state labeling pattern |

---

## Status History

| Date | Status | Notes |
|------|--------|-------|
| 2026-02-05 | Draft | Initial creation |
