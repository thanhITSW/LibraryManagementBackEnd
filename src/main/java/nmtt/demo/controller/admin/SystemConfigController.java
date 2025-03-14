package nmtt.demo.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.system.MaintenanceModeRequest;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.service.system.SystemConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${admin-mapping}/system-config")
@RequiredArgsConstructor
@Slf4j
public class SystemConfigController {
    private final SystemConfigService systemConfigService;

    @PostMapping("/maintenance")
    public ResponseEntity<SystemConfig> updateMaintenanceMode(@RequestBody MaintenanceModeRequest request) {

        try {
            SystemConfig updatedConfig = systemConfigService.updateMaintenanceMode(request.isMaintenanceMode());
            return ResponseEntity.ok(updatedConfig);
        } catch (Exception e) {
            log.error("Error updating maintenance mode", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}