package nmtt.demo.repository;

import nmtt.demo.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing, String> {
    boolean existsByAccountIdAndBookIdAndReturnedFalse(String accountId, String bookId);
    Optional<Borrowing> findByAccountIdAndBookIdAndReturnedFalse(String accountId, String bookId);
}
