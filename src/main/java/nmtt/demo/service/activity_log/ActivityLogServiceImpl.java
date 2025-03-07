package nmtt.demo.service.activity_log;

import lombok.RequiredArgsConstructor;
import nmtt.demo.entity.ActivityLog;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.ActivityLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService{

    private final ActivityLogRepository logRepository;
    @Override
    @Async
    public void log(String action, String entityType, String entityId,
                    String description, Map<String, Object> oldData, Map<String, Object> newData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String performedBy = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName() : "anonymous";

        Map<String, Object> filteredOldData = oldData;
        Map<String, Object> filteredNewData = newData;
        if ("UPDATE".equals(action) && oldData != null && newData != null) {
            filteredOldData = filterChangedFields(oldData, newData, true);
            filteredNewData = filterChangedFields(oldData, newData, false);
        }

        ActivityLog log = new ActivityLog(action, performedBy, entityType, entityId, description, filteredOldData, filteredNewData);
        logRepository.save(log);
    }

    private Map<String, Object> filterChangedFields(Map<String, Object> oldData, Map<String, Object> newData, boolean isOld) {
        return oldData.entrySet().stream()
                .filter(entry -> {
                    Object oldValue = entry.getValue();
                    Object newValue = newData.get(entry.getKey());
                    return (oldValue == null && newValue != null) ||
                            (oldValue != null && !oldValue.equals(newValue));
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Object value = isOld ? entry.getValue() : newData.get(entry.getKey());
                            return value != null ? value : "null";
                        },
                        (v1, v2) -> v1,
                        HashMap::new
                ));
    }

    @Override
    public List<ActivityLog> getAllActivityLogs() {
        return logRepository.findAll().stream().toList();
    }

    @Override
    public ActivityLog getActivityLogById(String id){

        return logRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_LOG_NOT_EXISTED));
    }
}
