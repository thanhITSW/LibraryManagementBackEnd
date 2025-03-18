package nmtt.demo.service.search.activity_log;

import lombok.Data;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.StringFilter;

import java.io.Serializable;

@Data
public class ActivityLogCriteria implements Serializable {
    private StringFilter action;
    private StringFilter performedBy;
    private StringFilter entityType;
    private StringFilter entityId;
    private StringFilter description;
    private InstantFilter timestamp;
}
