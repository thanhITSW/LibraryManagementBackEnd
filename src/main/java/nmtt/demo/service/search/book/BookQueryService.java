package nmtt.demo.service.search.book;

import lombok.RequiredArgsConstructor;
import nmtt.demo.entity.Book;
import nmtt.demo.entity.Book_;
import nmtt.demo.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

@Service
@Transactional
@RequiredArgsConstructor
public class BookQueryService extends QueryService<Book> {
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Page<Book> findByCriteria(BookCriteria criteria, Pageable pageable) {
        Specification<Book> specification = createSpecification(criteria);
        return bookRepository.findAll(specification, pageable);
    }

    private Specification<Book> createSpecification(BookCriteria criteria) {
        Specification<Book> spec = Specification.where(null);

        if (criteria.getTitle() != null) {
            spec = spec.and(buildStringSpecification(criteria.getTitle(), Book_.title));
        }

        if (criteria.getAuthor() != null) {
            spec = spec.and(buildStringSpecification(criteria.getAuthor(), Book_.author));
        }

        if (criteria.getCategory() != null) {
            spec = spec.and(buildStringSpecification(criteria.getCategory(), Book_.category));
        }

        if (criteria.getAvailable() != null) {
            spec = spec.and(buildSpecification(criteria.getAvailable(), Book_.available));
        }

        return spec;
    }
}
