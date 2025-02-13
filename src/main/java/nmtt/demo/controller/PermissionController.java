package nmtt.demo.controller;

import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.service.permission.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> create(@RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAll() {
        List<PermissionResponse> response = permissionService.getAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{permission}")
    public ResponseEntity<Void> delete(@PathVariable String permission) {
        permissionService.delete(permission);
        return ResponseEntity.noContent().build();
    }

}
