package nmtt.demo.repository;

import nmtt.demo.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing, String> {
    boolean existsByAccountIdAndBookIdAndReturnedFalse(String accountId, String bookId);
    Optional<Borrowing> findByAccountIdAndBookIdAndReturnedFalse(String accountId, String bookId);
    List<Borrowing> findByAccountId(String accountId);
    int countDistinctBookByBorrowDateBetween(LocalDate fromDate, LocalDate toDate);
    List<Borrowing> findByBorrowDateBetween(LocalDate fromDate, LocalDate toDate);
    boolean existsByBookId(String bookId);

    boolean existsByAccountId(String accountId);
}
