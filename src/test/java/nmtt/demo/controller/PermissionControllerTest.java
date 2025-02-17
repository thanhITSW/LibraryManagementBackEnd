package nmtt.demo.controller;

import nmtt.demo.dto.request.Account.PermissionRequest;
import nmtt.demo.dto.response.Account.PermissionResponse;
import nmtt.demo.service.permission.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PermissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    private PermissionRequest permissionRequest;
    private PermissionResponse permissionResponse;
    private List<PermissionResponse> permissionList;

    @BeforeEach
    void setUp() {
        permissionRequest = new PermissionRequest("ROLE_ADMIN", "Administrator role");
        permissionResponse = new PermissionResponse("ROLE_ADMIN", "Administrator role");

        permissionList = List.of(
                new PermissionResponse("ROLE_USER", "User role"),
                new PermissionResponse("ROLE_ADMIN", "Administrator role")
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePermission_Success() throws Exception {
        when(permissionService.create(any(PermissionRequest.class))).thenReturn(permissionResponse);

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"ROLE_ADMIN\",\"description\":\"Administrator role\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.permission").value("Administrator role"));

        verify(permissionService, times(1)).create(any(PermissionRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllPermissions_Success() throws Exception {
        when(permissionService.getAll()).thenReturn(permissionList);

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("ROLE_USER"))
                .andExpect(jsonPath("$[1].name").value("ROLE_ADMIN"));

        verify(permissionService, times(1)).getAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeletePermission_Success() throws Exception {
        doNothing().when(permissionService).delete("ROLE_USER");

        mockMvc.perform(delete("/permissions/ROLE_USER"))
                .andExpect(status().isNoContent());

        verify(permissionService, times(1)).delete("ROLE_USER");
    }
}
