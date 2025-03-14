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

    /**
     * Logs an activity with the provided details.
     *
     * @param action The type of action performed (e.g., CREATE, UPDATE, DELETE).
     * @param entityType The type of entity being logged (e.g., USER, PRODUCT).
     * @param entityId The unique identifier of the entity.
     * @param description A brief description of the activity.
     * @param oldData The original data before the update (only applicable for UPDATE action).
     * @param newData The updated data after the update (only applicable for UPDATE action).
     *
     * This method logs the activity by creating an {@link ActivityLog} object and saving it to the database.
     * If the action is UPDATE, it filters out the changed fields from the oldData and newData and only logs those changes.
     * The performedBy field is set to the authenticated user's username if available, otherwise it defaults to "anonymous".
     */
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


    /**
     * Filters the changed fields between two maps based on the provided flag.
     *
     * @param oldData The original data map.
     * @param newData The updated data map.
     * @param isOld Indicates whether to return the old values (true) or new values (false) for changed fields.
     * @return A map containing only the changed fields and their corresponding values based on the provided flag.
     *
     * This method filters the changed fields between the oldData and newData maps.
     * It checks for null values and non-equal values between corresponding keys in the two maps.
     * If the isOld flag is true, it returns the old values for changed fields; otherwise, it returns the new values.
     * If a value is null in either map, it is represented as "null" in the resulting map.
     */
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

    /**
     * Retrieves all activity logs.
     *
     * @return List of all activity logs.
     */
    @Override
    public List<ActivityLog> getAllActivityLogs() {
        return logRepository.findAll().stream().toList();
    }

    /**
     * Retrieves an activity log by its ID.
     *
     * @param id The ID of the activity log.
     * @return The activity log with the specified ID.
     * @throws AppException if the activity log does not exist.
     */
    @Override
    public ActivityLog getActivityLogById(String id){

        return logRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_LOG_NOT_EXISTED));
    }
}
