package nmtt.demo.controller;

import nmtt.demo.entity.SystemConfig;
import nmtt.demo.service.system.SystemConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SystemConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemConfigService systemConfigService;

    private SystemConfig systemConfig;

    @BeforeEach
    void setUp() {
        // Mock SystemConfig object
        systemConfig = new SystemConfig(1L, true);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetCurrentConfig_Success() throws Exception {
        when(systemConfigService.getConfig()).thenReturn(systemConfig);

        mockMvc.perform(get("/system-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maintenanceMode").value(true));

        verify(systemConfigService, times(1)).getConfig();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetCurrentConfig_NotFound() throws Exception {
        when(systemConfigService.getConfig()).thenReturn(null);

        mockMvc.perform(get("/system-config"))
                .andExpect(status().isNotFound());

        verify(systemConfigService, times(1)).getConfig();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateMaintenanceMode_Success() throws Exception {
        when(systemConfigService.updateMaintenanceMode(true)).thenReturn(systemConfig);

        mockMvc.perform(post("/system-config/maintenance")
                        .param("maintenanceMode", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maintenanceMode").value(true));

        verify(systemConfigService, times(1)).updateMaintenanceMode(true);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateMaintenanceMode_InternalServerError() throws Exception {
        when(systemConfigService.updateMaintenanceMode(true)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/system-config/maintenance")
                        .param("maintenanceMode", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(systemConfigService, times(1)).updateMaintenanceMode(true);
    }

}
