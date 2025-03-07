package nmtt.demo.service.activity_log;

import nmtt.demo.entity.ActivityLog;

import java.util.List;
import java.util.Map;

public interface ActivityLogService {
    void log(String action, String entityType, String entityId,
                    String description, Map<String, Object> oldData, Map<String, Object> newData);

    List<ActivityLog> getAllActivityLogs();
    ActivityLog getActivityLogById(String id);
}
