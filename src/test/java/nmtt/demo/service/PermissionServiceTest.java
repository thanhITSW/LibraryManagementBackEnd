package nmtt.demo.service;

import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.entity.Permission;
import nmtt.demo.mapper.PermissionMapper;
import nmtt.demo.repository.PermissionRepository;
import nmtt.demo.service.permission.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class PermissionServiceTest {
    @MockBean
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionMapper permissionMapper;
    @Autowired
    private PermissionService permissionService;

    private Permission permission;
    private PermissionRequest permissionRequest;
    private PermissionResponse permissionResponse;

    @BeforeEach
    void setUp() {
        permission = new Permission("READ_ACCESS", "Allows read access");

        permissionRequest = new PermissionRequest("READ_ACCESS", "Allows read access");

        permissionResponse = new PermissionResponse("READ_ACCESS", "Allows read access");
    }

    @Test
    void testCreatePermission() {
        when(permissionMapper.toPermission(any(PermissionRequest.class))).thenReturn(permission);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);
        when(permissionMapper.toPermissionResponse(any(Permission.class))).thenReturn(permissionResponse);

        PermissionResponse response = permissionService.create(permissionRequest);

        assertNotNull(response);
        assertEquals("READ_ACCESS", response.getName());
        assertEquals("Allows read access", response.getPermission());
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    void testGetAllPermissions() {
        when(permissionRepository.findAll()).thenReturn(List.of(permission));
        when(permissionMapper.toPermissionResponse(any(Permission.class))).thenReturn(permissionResponse);

        List<PermissionResponse> responses = permissionService.getAll();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("READ_ACCESS", responses.get(0).getName());
        assertEquals("Allows read access", responses.get(0).getPermission());
    }

    @Test
    void testDeletePermission() {
        doNothing().when(permissionRepository).deleteById("READ_ACCESS");

        permissionService.delete("READ_ACCESS");

        verify(permissionRepository, times(1)).deleteById("READ_ACCESS");
    }
}
