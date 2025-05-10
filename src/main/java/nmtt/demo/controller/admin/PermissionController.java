package nmtt.demo.controller.admin;

import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.service.permission.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("${admin-mapping}/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    /**
     * Creates a new permission.
     *
     * @param request The request containing permission details.
     * @return The created permission response with HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<PermissionResponse> create(@RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all permissions.
     *
     * @return A list of all permissions with HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAll() {
        List<PermissionResponse> response = permissionService.getAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a permission by its name.
     *
     * @param permission The name of the permission to delete.
     * @return HTTP status 204 (No Content) if the deletion is successful.
     */
    @DeleteMapping("/{permission}")
    public ResponseEntity<Void> delete(@PathVariable String permission) {
        permissionService.delete(permission);
        return ResponseEntity.noContent().build();
    }
}
