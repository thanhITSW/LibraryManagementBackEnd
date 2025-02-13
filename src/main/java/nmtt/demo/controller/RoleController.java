package nmtt.demo.controller;

import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.RoleResponse;
import nmtt.demo.service.role.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponse> create(@RequestBody RoleRequest request) {
        RoleResponse roleResponse = roleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(roleResponse);
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAll() {
        List<RoleResponse> roles = roleService.getAll();
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/{role}")
    public ResponseEntity<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
