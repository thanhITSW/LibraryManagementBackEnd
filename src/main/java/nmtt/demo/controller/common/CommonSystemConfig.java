package nmtt.demo.controller.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.SystemConfig;
import nmtt.demo.service.system.SystemConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${common-mapping}/system-config")
@RequiredArgsConstructor
@Slf4j
public class CommonSystemConfig {
    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<SystemConfig> getCurrentConfig() {
        SystemConfig config = systemConfigService.getConfig();

        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(config);
    }
}
