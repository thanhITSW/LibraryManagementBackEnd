package nmtt.demo.service;

import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.RoleResponse;
import nmtt.demo.entity.Permission;
import nmtt.demo.entity.Role;
import nmtt.demo.mapper.RoleMapper;
import nmtt.demo.repository.PermissionRepository;
import nmtt.demo.repository.RoleRepository;
import nmtt.demo.service.role.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class RoleServiceTest {
    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PermissionRepository permissionRepository;

    @Mock
    private RoleMapper roleMapper;

    @Autowired
    private RoleService roleService;

    private Role role;
    private RoleRequest roleRequest;
    private RoleResponse roleResponse;
    private Permission permission;

    @BeforeEach
    void setUp() {
        permission = new Permission("READ_ACCESS", "Allows read access");
        role = new Role("ADMIN", "Administrator role", Set.of(permission));
        roleRequest = new RoleRequest("ADMIN", "Administrator role", Set.of("READ_ACCESS"));
        roleResponse = new RoleResponse("ADMIN", "Administrator role", Set.of());
    }

    @Test
    void testCreateRole() {
        when(roleMapper.toRole(any(RoleRequest.class))).thenReturn(role);
        when(permissionRepository.findAllById(anySet())).thenReturn(List.of(permission));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toRoleResponse(any(Role.class))).thenReturn(roleResponse);

        RoleResponse response = roleService.create(roleRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getName());
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testGetAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(roleMapper.toRoleResponse(any(Role.class))).thenReturn(roleResponse);

        List<RoleResponse> responses = roleService.getAll();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("ADMIN", responses.get(0).getName());
    }

    @Test
    void testDeleteRole() {
        doNothing().when(roleRepository).deleteById("ADMIN");

        roleService.delete("ADMIN");

        verify(roleRepository, times(1)).deleteById("ADMIN");
    }
}
