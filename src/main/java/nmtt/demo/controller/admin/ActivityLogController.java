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

    /**
     * Retrieves a list of all activity logs.
     *
     * @return a ResponseEntity containing a list of ActivityLog objects.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     */
    @GetMapping
    public ResponseEntity<List<ActivityLog>> getAllUsers() {
        List<ActivityLog> activityLogs = activityLogService.getAllActivityLogs();

        return ResponseEntity.ok(activityLogs);
    }

    /**
     * Retrieves an activity log by its unique identifier.
     *
     * @param activityId the unique identifier of the activity log to retrieve.
     * @return a ResponseEntity containing the requested ActivityLog object.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     *         If the activity log with the given ID does not exist, the HTTP status code is 404 (Not Found).
     */
    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityLog> getActivityById(@PathVariable String activityId) {
        ActivityLog activityLog = activityLogService.getActivityLogById(activityId);

        return ResponseEntity.ok(activityLog);
    }

    /**
     * Performs a search operation on activity logs based on the provided criteria and pagination parameters.
     *
     * @param criteria the search criteria to filter activity logs.
     * @param pageable the pagination parameters to limit the number of results returned.
     * @return a ResponseEntity containing a Page of ActivityLog objects that match the search criteria.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     *         If no activity logs match the search criteria, an empty Page is returned.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ActivityLog>> searchActivityLog(ActivityLogCriteria criteria
            , Pageable pageable) {

        Page<ActivityLog> result = activityQueryService.findByCriteria(criteria, pageable);
        return ResponseEntity.ok(result);
    }
}
