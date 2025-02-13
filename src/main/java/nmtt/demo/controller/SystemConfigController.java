package nmtt.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.service.system.SystemConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system-config")
@RequiredArgsConstructor
@Slf4j
public class SystemConfigController {
    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<SystemConfig> getCurrentConfig() {
        SystemConfig config = systemConfigService.getConfig();

        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(config);
    }

    @PostMapping("/maintenance")
    public ResponseEntity<SystemConfig> updateMaintenanceMode(@RequestParam boolean maintenanceMode) {
        log.info("Updating maintenance mode to: " + maintenanceMode);

        try {
            SystemConfig updatedConfig = systemConfigService.updateMaintenanceMode(maintenanceMode);
            return ResponseEntity.ok(updatedConfig);
        } catch (Exception e) {
            log.error("Error updating maintenance mode", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
