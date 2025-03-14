package nmtt.demo.service.system;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.SystemConfigRepository;
import nmtt.demo.service.activity_log.ActivityLogService;
import nmtt.demo.service.email.EmailSenderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService{
    private final SystemConfigRepository systemConfigRepository;
    private final AccountRepository accountRepository;
    private final EmailSenderService emailSenderService;
    private final ActivityLogService logService;

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
        HashMap<String, Object> oldData = toMap(config);

        config.setMaintenanceMode(maintenanceMode);

        if (maintenanceMode) {
            List<Account> users = accountRepository.findAll();
            String subject = "Notice: System is under maintenance";
            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                    + "<h2 style='color: #d9534f;'>System Maintenance Notification</h2>"
                    + "<p>Hello,</p>"
                    + "<p>We would like to inform you that our system is currently undergoing maintenance to improve performance and security.</p>"
                    + "<p>During this period, some features may be temporarily unavailable. We apologize for any inconvenience and appreciate your patience.</p>"
                    + "<p>Thank you for your understanding and support.</p>"
                    + "<p>Best regards,<br>The SparkMinds Team</p>"
                    + "</div>";


            for (Account user : users) {
                boolean isUserRole = user.getRoles().stream()
                        .anyMatch(role -> "USER".equalsIgnoreCase(role.getName()));

                if (isUserRole) {
                    emailSenderService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
                }
            }
        }

        SystemConfig updatedConfig = systemConfigRepository.save(config);

        HashMap<String, Object> newData = toMap(updatedConfig);
        logService.log("UPDATE", "SYSTEM_CONFIG", updatedConfig.getId().toString(),
                "Admin updated maintenance mode", oldData, newData);

        return updatedConfig;
    }

    /**
     * Converts a SystemConfig object into a HashMap for logging purposes.
     *
     * @param config The SystemConfig object to be converted.
     * @return A HashMap containing the SystemConfig object's properties.
     * The returned HashMap contains the following keys and their corresponding values:
     * - "id": The ID of the SystemConfig object.
     * - "maintenanceMode": A boolean indicating whether the system is in maintenance mode.
     */
    private HashMap<String, Object> toMap(SystemConfig config) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", config.getId());
        data.put("maintenanceMode", config.isMaintenanceMode());
        return data;
    }
}
