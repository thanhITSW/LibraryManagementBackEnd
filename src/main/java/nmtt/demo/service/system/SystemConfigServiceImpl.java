package nmtt.demo.service.system;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.SystemConfigRepository;
import nmtt.demo.service.email.EmailSenderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService{
    private final SystemConfigRepository systemConfigRepository;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;

    /**
     * Retrieves the system configuration.
     * If no configuration exists, creates and saves a default configuration.
     *
     * @return The system configuration.
     */
    @Override
    public SystemConfig getConfig() {
        return systemConfigRepository.findById(1L).orElseGet(() -> {
            SystemConfig defaultConfig = SystemConfig
                    .builder()
                    .maintenanceMode(false)
                    .build();
            return systemConfigRepository.save(defaultConfig);
        });
    }

    /**
     * Updates the maintenance mode setting of the system.
     * If maintenance mode is enabled, it sends an email to all users notifying them of the maintenance.
     *
     * @param maintenanceMode A boolean indicating whether the system should be in maintenance mode.
     * @return The updated system configuration.
     */
    @Transactional
    @Override
    public SystemConfig updateMaintenanceMode(boolean maintenanceMode) {
        SystemConfig config = getConfig();
        config.setMaintenanceMode(maintenanceMode);

        if (maintenanceMode) {
            List<Account> users = accountRepository.findAll();
            String subject = "Thông báo: Hệ thống đang bảo trì";
            String message = """
            Xin chào,

            Hệ thống sẽ được bảo trì trong thời gian sắp tới. Chúng tôi sẽ thông báo khi hệ thống hoạt động trở lại.

            Trân trọng,
            Đội ngũ quản trị.
            """;

            for (Account user : users) {
                emailSenderService.sendSimpleEmail(user.getEmail(), subject, message);
            }
        }

        return systemConfigRepository.save(config);
    }
}
