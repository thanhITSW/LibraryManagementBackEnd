package nmtt.demo.service.search.activity_log;

import lombok.RequiredArgsConstructor;
import nmtt.demo.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import tech.jhipster.service.filter.*;

import java.util.List;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ActivityQueryService {
    private final MongoTemplate mongoTemplate;

    public Page<ActivityLog> findByCriteria(ActivityLogCriteria criteria, Pageable pageable) {
        Query query = createQuery(criteria);
        long total = mongoTemplate.count(query, ActivityLog.class);

        query.with(pageable);
        List<ActivityLog> results = mongoTemplate.find(query, ActivityLog.class);

        return new PageImpl<>(results, pageable, total);
    }

    private Query createQuery(ActivityLogCriteria criteria) {
        Query query = new Query();

        if (criteria.getAction() != null) {
            query.addCriteria(buildStringCriteria(criteria.getAction(), "action"));
        }
        if (criteria.getPerformedBy() != null) {
            query.addCriteria(buildStringCriteria(criteria.getPerformedBy(), "performedBy"));
        }
        if (criteria.getEntityType() != null) {
            query.addCriteria(buildStringCriteria(criteria.getEntityType(), "entityType"));
        }
        if (criteria.getEntityId() != null) {
            query.addCriteria(buildStringCriteria(criteria.getEntityId(), "entityId"));
        }
        if (criteria.getDescription() != null) {
            query.addCriteria(buildStringCriteria(criteria.getDescription(), "description"));
        }
        if (criteria.getTimestamp() != null) {
            query.addCriteria(buildRangeCriteria(criteria.getTimestamp(), "timestamp"));
        }

        return query;
    }

    private Criteria buildStringCriteria(StringFilter filter, String field) {
        Criteria criteria = Criteria.where(field);
        if (filter.getEquals() != null) {
            criteria.is(filter.getEquals());
        } else if (filter.getContains() != null) {
            criteria.regex(".*" + filter.getContains() + ".*", "i"); // Case-insensitive
        } else if (filter.getDoesNotContain() != null) {
            criteria.not().regex(".*" + filter.getDoesNotContain() + ".*", "i");
        } else if (filter.getIn() != null && !filter.getIn().isEmpty()) {
            criteria.in(filter.getIn());
        } else if (filter.getNotIn() != null && !filter.getNotIn().isEmpty()) {
            criteria.nin(filter.getNotIn());
        }
        return criteria;
    }

    private Criteria buildRangeCriteria(InstantFilter filter, String field) {
        Criteria criteria = Criteria.where(field);
        if (filter.getGreaterThan() != null) {
            criteria.gt(filter.getGreaterThan());
        }
        if (filter.getGreaterThanOrEqual() != null) {
            criteria.gte(filter.getGreaterThanOrEqual());
        }
        if (filter.getLessThan() != null) {
            criteria.lt(filter.getLessThan());
        }
        if (filter.getLessThanOrEqual() != null) {
            criteria.lte(filter.getLessThanOrEqual());
        }
        return criteria;
    }
}
