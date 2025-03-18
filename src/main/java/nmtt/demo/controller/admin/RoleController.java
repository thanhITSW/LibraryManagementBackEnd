package nmtt.demo.controller.admin;

import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.RoleResponse;
import nmtt.demo.service.role.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${admin-mapping}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    /**
     * Creates a new role.
     *
     * @param request The request containing role details.
     * @return The created role response with HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<RoleResponse> create(@RequestBody RoleRequest request) {
        RoleResponse roleResponse = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(roleResponse);
    }

    /**
     * Retrieves all roles.
     *
     * @return A list of all roles with HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAll() {
        List<RoleResponse> roles = roleService.getAll();
        return ResponseEntity.ok(roles);
    }

    /**
     * Deletes a role by its name.
     *
     * @param role The name of the role to delete.
     * @return HTTP status 204 (No Content) if the deletion is successful.
     */
    @DeleteMapping("/{role}")
    public ResponseEntity<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
