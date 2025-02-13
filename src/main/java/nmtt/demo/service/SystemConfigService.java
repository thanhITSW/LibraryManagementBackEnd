package nmtt.demo.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.SystemConfigRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SystemConfigService {
    SystemConfigRepository systemConfigRepository;
    AccountRepository accountRepository;
    EmailSenderService emailSenderService;

    public SystemConfig getConfig() {
        return systemConfigRepository.findById(1L).orElseGet(() -> {
            SystemConfig defaultConfig = SystemConfig.builder()
                    .maintenanceMode(false)
                    .build();
            return systemConfigRepository.save(defaultConfig);
        });
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
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
