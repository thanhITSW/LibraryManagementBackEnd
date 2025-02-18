package nmtt.demo.service;

import nmtt.demo.entity.Account;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.SystemConfigRepository;
import nmtt.demo.service.email.EmailSenderService;
import nmtt.demo.service.system.SystemConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class SystemConfigServiceTest {
    @MockBean
    private SystemConfigRepository systemConfigRepository;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private EmailSenderService emailSenderService;

    @Autowired
    private SystemConfigService systemConfigService;

    private SystemConfig systemConfig;

    @BeforeEach
    void setUp() {
        systemConfig = SystemConfig.builder().id(1L).maintenanceMode(false).build();
    }

    @Test
    void testGetConfig_WhenConfigExists() {
        when(systemConfigRepository.findById(1L)).thenReturn(Optional.of(systemConfig));

        SystemConfig config = systemConfigService.getConfig();

        assertNotNull(config);
        assertFalse(config.isMaintenanceMode());
        verify(systemConfigRepository, times(1)).findById(1L);
    }

    @Test
    void testGetConfig_WhenConfigDoesNotExist() {
        when(systemConfigRepository.findById(1L)).thenReturn(Optional.empty());
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(systemConfig);

        SystemConfig config = systemConfigService.getConfig();

        assertNotNull(config);
        assertFalse(config.isMaintenanceMode());
        verify(systemConfigRepository, times(1)).save(any(SystemConfig.class));
    }

    @Test
    void testUpdateMaintenanceMode_Disabled() {
        when(systemConfigRepository.findById(1L)).thenReturn(Optional.of(systemConfig));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(systemConfig);

        SystemConfig updatedConfig = systemConfigService.updateMaintenanceMode(false);

        assertNotNull(updatedConfig);
        assertFalse(updatedConfig.isMaintenanceMode());
        verify(systemConfigRepository, times(1)).save(any(SystemConfig.class));
        verify(emailSenderService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateMaintenanceMode_SendsEmail() {
        when(systemConfigRepository.findById(1L)).thenReturn(Optional.of(systemConfig));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenReturn(systemConfig);

        Account user1 = new Account();
        user1.setEmail("john1@gmail.com");
        Account user2 = new Account();
        user2.setEmail("john2@gmail.com");

        when(accountRepository.findAll()).thenReturn(List.of(user1, user2));

        SystemConfig updatedConfig = systemConfigService.updateMaintenanceMode(true);

        assertNotNull(updatedConfig);
        assertTrue(updatedConfig.isMaintenanceMode());
        verify(systemConfigRepository, times(1)).save(any(SystemConfig.class));
        verify(emailSenderService, times(2)).sendSimpleEmail(anyString(), anyString(), anyString());
    }
}
