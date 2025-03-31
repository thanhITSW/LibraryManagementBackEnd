package nmtt.demo.service.account;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import nmtt.demo.entity.Account;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AccountSearchSpecification {
    public static Specification<Account> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) return null;

            Predicate firstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), "%" + name.toLowerCase() + "%"
            );
            Predicate lastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), "%" + name.toLowerCase() + "%"
            );

            return criteriaBuilder.or(firstNamePredicate, lastNamePredicate);
        };
    }

    public static Specification<Account> hasBookTitle(String bookTitle) {
        return (root, query, criteriaBuilder) -> {
            if (bookTitle == null || bookTitle.isEmpty()) return null;

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("borrowings", JoinType.LEFT).get("book").get("title")),
                    "%" + bookTitle.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Account> isBornInDateRange(String dateFrom, String dateTo) {
        return (root, query, criteriaBuilder) -> {
            if (dateFrom == null || dateTo == null) return null;

            try {
                LocalDate fromDate = LocalDate.parse(dateFrom);
                LocalDate toDate = LocalDate.parse(dateTo);
                return criteriaBuilder.between(root.get("dob"), fromDate, toDate);
            } catch (DateTimeParseException e) {
                return null;
            }
        };
    }
}
