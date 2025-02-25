package nmtt.demo.service.search.account;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Account_;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.entity.Book;
import nmtt.demo.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tech.jhipster.service.QueryService;

@Service
@RequiredArgsConstructor
public class AccountQueryService extends QueryService<Account>{
    private final AccountRepository accountRepository;

    public Page<Account> findByCriteria(AccountCriteria criteria, Pageable pageable) {
        Specification<Account> specification = createSpecification(criteria);
        return accountRepository.findAll(specification, pageable);
    }

    private Specification<Account> createSpecification(AccountCriteria criteria) {
        Specification<Account> spec = Specification.where(null);

        if (criteria.getFirstName() != null) {
            spec = spec.and(buildStringSpecification(criteria.getFirstName(), Account_.firstName));
        }

        if (criteria.getLastName() != null) {
            spec = spec.and(buildStringSpecification(criteria.getLastName(), Account_.lastName));
        }

        if (criteria.getDob() != null) {
            spec = spec.and(buildRangeSpecification(criteria.getDob(), Account_.dob));
        }

        if (criteria.getPhone() != null) {
            spec = spec.and(buildStringSpecification(criteria.getPhone(), Account_.phone));
        }

        if (criteria.getActive() != null) {
            spec = spec.and(buildSpecification(criteria.getActive(), Account_.active));
        }

        if (criteria.getBookTitle() != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Account, Borrowing> borrowingJoin = root.join("borrowings");
                Join<Borrowing, Book> bookJoin = borrowingJoin.join("book");
                return buildSpecification(criteria.getBookTitle(),
                        book -> cb.lower(bookJoin.get("title"))).toPredicate(root, query, cb);
            });
        }

        return spec;
    }
}
