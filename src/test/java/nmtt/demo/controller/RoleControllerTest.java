package nmtt.demo.controller;

import nmtt.demo.dto.request.Account.RoleRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.dto.response.Account.RoleResponse;
import nmtt.demo.service.role.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class RoleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    private RoleRequest roleRequest;
    private RoleResponse roleResponse;
    private List<RoleResponse> roleList;

    @BeforeEach
    void setUp() {
        Set<String> requestPermissions = Set.of("READ_PRIVILEGES", "WRITE_PRIVILEGES");
        Set<PermissionResponse> responsePermissions = Set.of(
                new PermissionResponse("READ_PRIVILEGES", "Allows read access"),
                new PermissionResponse("WRITE_PRIVILEGES", "Allows write access")
        );

        roleRequest = new RoleRequest("ROLE_ADMIN", "Administrator role", requestPermissions);
        roleResponse = new RoleResponse("ROLE_ADMIN", "Administrator role", responsePermissions);

        roleList = List.of(
                new RoleResponse("ROLE_USER", "User role", responsePermissions),
                new RoleResponse("ROLE_ADMIN", "Administrator role", responsePermissions)
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateRole_Success() throws Exception {
        when(roleService.create(any(RoleRequest.class))).thenReturn(roleResponse);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ROLE_ADMIN\",\"description\":\"Administrator role\",\"permissions\":[\"READ_PRIVILEGES\",\"WRITE_PRIVILEGES\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.description").value("Administrator role"))
                .andExpect(jsonPath("$.permissions.size()").value(2))
                .andExpect(jsonPath("$.permissions[?(@.name == 'READ_PRIVILEGES')]").exists())
                .andExpect(jsonPath("$.permissions[?(@.name == 'WRITE_PRIVILEGES')]").exists());

        verify(roleService, times(1)).create(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllRoles_Success() throws Exception {
        when(roleService.getAll()).thenReturn(roleList);

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("ROLE_USER"))
                .andExpect(jsonPath("$[1].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[0].permissions.size()").value(2))
                .andExpect(jsonPath("$[1].permissions.size()").value(2));

        verify(roleService, times(1)).getAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteRole_Success() throws Exception {
        doNothing().when(roleService).delete("ROLE_USER");

        mockMvc.perform(delete("/roles/ROLE_USER"))
                .andExpect(status().isNoContent());

        verify(roleService, times(1)).delete("ROLE_USER");
    }
}
