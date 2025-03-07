package nmtt.demo.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.ActivityLog;
import nmtt.demo.service.activity_log.ActivityLogService;
import nmtt.demo.service.search.activity_log.ActivityLogCriteria;
import nmtt.demo.service.search.activity_log.ActivityQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${admin-mapping}/activity-log")
@Slf4j
@RequiredArgsConstructor
public class ActivityLogController {
    private final ActivityLogService activityLogService;
    private final ActivityQueryService activityQueryService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getAllUsers() {
        List<ActivityLog> activityLogs = activityLogService.getAllActivityLogs();

        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityLog> getActivityById(@PathVariable String activityId) {
        ActivityLog activityLog = activityLogService.getActivityLogById(activityId);

        return ResponseEntity.ok(activityLog);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ActivityLog>> searchActivityLog(ActivityLogCriteria criteria
            , Pageable pageable) {

        Page<ActivityLog> result = activityQueryService.findByCriteria(criteria, pageable);
        return ResponseEntity.ok(result);
    }
}
