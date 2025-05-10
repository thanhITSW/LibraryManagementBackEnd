package nmtt.demo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "activity_logs")
@Data
public class ActivityLog {
    @Id
    private String id;
    private String action;
    private String performedBy;
    private String entityType;
    private String entityId;
    private String description;
    private LocalDateTime timestamp;
    private Map<String, Object> oldData;
    private Map<String, Object> newData;

    public ActivityLog(String action, String performedBy, String entityType, String entityId,
                       String description, Map<String, Object> oldData, Map<String, Object> newData) {
        this.action = action;
        this.performedBy = performedBy;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.oldData = oldData;
        this.newData = newData;
        this.timestamp = LocalDateTime.now();
    }
}
