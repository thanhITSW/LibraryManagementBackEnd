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

    /**
     * This function retrieves a paginated list of ActivityLog entities based on the provided criteria and pageable settings.
     * It uses MongoDB's query capabilities to filter and sort the results.
     *
     * @param criteria The ActivityLogCriteria object containing the filtering criteria.
     * @param pageable The Pageable object specifying the pagination settings (page number, page size, sorting).
     * @return A Page object containing the list of ActivityLog entities that match the criteria and are paginated.
     */
    public Page<ActivityLog> findByCriteria(ActivityLogCriteria criteria, Pageable pageable) {
        Query query = createQuery(criteria);
        long total = mongoTemplate.count(query, ActivityLog.class);

        query.with(pageable);
        List<ActivityLog> results = mongoTemplate.find(query, ActivityLog.class);

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * This function constructs a MongoDB query based on the provided ActivityLogCriteria.
     * It iterates through the criteria properties and adds corresponding criteria to the query.
     *
     * @param criteria The ActivityLogCriteria object containing the filtering criteria.
     * @return A Query object representing the constructed MongoDB query.
     */
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

    /**
     * This function constructs a MongoDB criteria for string-based filtering.
     * It handles different types of string filters (equals, contains, does not contain, in, not in)
     * and applies them to the specified field.
     *
     * @param filter The StringFilter object containing the filtering criteria.
     * @param field The name of the field to apply the criteria to.
     * @return A Criteria object representing the constructed MongoDB criteria.
     */
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

    /**
     * This function constructs a MongoDB criteria for range-based filtering based on the provided InstantFilter.
     * It handles different types of range filters (greater than, greater than or equal, less than, less than or equal)
     * and applies them to the specified field.
     *
     * @param filter The InstantFilter object containing the filtering criteria.
     * @param field The name of the field to apply the criteria to.
     * @return A Criteria object representing the constructed MongoDB criteria.
     */
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
