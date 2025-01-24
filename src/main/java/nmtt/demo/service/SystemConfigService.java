package nmtt.demo.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SystemConfigService {
    SystemConfigRepository systemConfigRepository;

    public SystemConfig getConfig() {
        return systemConfigRepository.findById(1L).orElseGet(() -> {
            SystemConfig defaultConfig = SystemConfig.builder()
                    .maintenanceMode(false)
                    .build();
            return systemConfigRepository.save(defaultConfig);
        });
    }

    @Transactional
    public SystemConfig updateMaintenanceMode(boolean maintenanceMode) {
        SystemConfig config = getConfig();
        config.setMaintenanceMode(maintenanceMode);
        return systemConfigRepository.save(config);
    }

}
