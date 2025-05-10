package nmtt.demo.service.book;

import jakarta.persistence.criteria.Predicate;
import nmtt.demo.entity.Book;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {
    public static Specification<Book> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String lowerKeyword = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), lowerKeyword));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), lowerKeyword));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), lowerKeyword));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
