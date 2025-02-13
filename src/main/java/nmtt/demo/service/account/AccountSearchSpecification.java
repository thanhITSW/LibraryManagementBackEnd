package nmtt.demo.service.account;

import jakarta.persistence.criteria.Predicate;
import nmtt.demo.entity.Account;
import org.springframework.data.jpa.domain.Specification;

public class AccountSearchSpecification {
    public static Specification<Account> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + name.toLowerCase() + "%");
            Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + name.toLowerCase() + "%");
            return criteriaBuilder.or(firstNamePredicate, lastNamePredicate);
        };
    }

    public static Specification<Account> hasBookTitle(String bookTitle) {
        return (root, query, criteriaBuilder) -> {
            if (bookTitle == null || bookTitle.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.join("borrowings").get("book").get("title")), "%" + bookTitle.toLowerCase() + "%");
        };
    }

    public static Specification<Account> isBornInDateRange(String dateFrom, String dateTo) {
        return (root, query, criteriaBuilder) -> {
            if (dateFrom == null || dateTo == null) {
                return criteriaBuilder.conjunction();
            }
            Predicate datePredicate = criteriaBuilder.between(root.get("dob"), dateFrom, dateTo);
            return datePredicate;
        };
    }
}
