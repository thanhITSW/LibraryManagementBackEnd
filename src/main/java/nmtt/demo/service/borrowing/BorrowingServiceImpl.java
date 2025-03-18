package nmtt.demo.service.borrowing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Book.BookRequest;
import nmtt.demo.dto.response.Book.BorrowBookResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.BookRepository;
import nmtt.demo.repository.BorrowingRepository;
import nmtt.demo.service.activity_log.ActivityLogService;
import nmtt.demo.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingServiceImpl implements BorrowingService{
    private final BorrowingRepository borrowingRepository;
    private final AccountRepository accountRepository;
    private final BookRepository bookRepository;
    private final ActivityLogService logService;

    /**
     * Allows a user to borrow a book if available.
     *
     * @param request the request containing the book ID to borrow
     * @throws AppException if the user does not exist, the book does not exist,
     *                      the user has already borrowed the book, or the book is unavailable.
     */
    @Transactional
    @Override
    public void borrowBook(BookRequest request){
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
        Account account = accountRepository
                .findById(issuer)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Book book = bookRepository
                .findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Check if the user has borrowed this type of book yet
        boolean alreadyBorrowed = borrowingRepository
                .existsByAccountIdAndBookIdAndReturnedFalse(issuer, request.getBookId());

        if(alreadyBorrowed){
            throw new AppException(ErrorCode.BORROWED_BOOK);
        }

        if(book.getAvailableCopies() < 1){
            throw new AppException(ErrorCode.NOT_AVAILABLE_BOOK);
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Borrowing borrowing = Borrowing.builder()
                .account(account)
                .book(book)
                .borrowDate(LocalDate.now())
                .returned(false)
                .build();

        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        HashMap<String, Object> newData = toMap(savedBorrowing);
        logService.log("BORROW_BOOK", "BORROWING", savedBorrowing.getId(),
                "User borrowed a book", null, newData);
    }

    /**
     * Processes the return of a borrowed book by updating the borrowing record and
     * increasing the available copies of the book.
     *
     * @param request the request containing the book ID to return
     * @throws AppException if no active borrowing record is found for the user and book.
     */
    @Transactional
    @Override
    public void returnBook(BookRequest request){
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
        Borrowing borrowing = borrowingRepository
                .findByAccountIdAndBookIdAndReturnedFalse(issuer, request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_RECORD_NOT_FOUND));

        HashMap<String, Object> oldData = toMap(borrowing);

        //Update status book
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());
        Borrowing updatedBorrowing = borrowingRepository.save(borrowing);

        //Increase the number of books available
        Book book = updatedBorrowing.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        HashMap<String, Object> newData = toMap(updatedBorrowing);
        logService.log("UPDATE", "BORROWING", borrowing.getId(),
                "User returned a book", oldData, newData);
    }

    /**
     * Retrieves a list of books currently borrowed by the authenticated user.
     * This method fetches all active borrowings (not returned) for the current user
     * and returns a list of the associated books.
     *
     * @return A List of Book objects representing the books currently borrowed by the user.
     *         The list will be empty if the user has no active borrowings.
     * @throws AssertionError if the issuer (authenticated user) is null.
     */
    @Transactional
    @Override
    public List<BorrowBookResponse> getBorrowedBooks() {
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
        List<Borrowing> borrowings = borrowingRepository
                .findByAccountId(issuer);

        return borrowings.stream().map(borrowing -> BorrowBookResponse.builder()
                .id(borrowing.getId())
                .bookId(borrowing.getBook().getId())
                .title(borrowing.getBook().getTitle())
                .borrowDate(borrowing.getBorrowDate())
                .returnDate(borrowing.getReturnDate())
                .returned(borrowing.isReturned())
                .build()).collect(Collectors.toList());
    }

    /**
     * Converts a Borrowing entity into a HashMap for logging purposes.
     *
     * @param borrowing The Borrowing entity to be converted.
     * @return A HashMap containing the relevant information from the Borrowing entity.
     *         The keys in the HashMap correspond to the field names in the Borrowing entity,
     *         and the values correspond to the field values.
     */
    private HashMap<String, Object> toMap(Borrowing borrowing) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", borrowing.getId());
        data.put("accountId", borrowing.getAccount().getId());
        data.put("bookId", borrowing.getBook().getId());
        data.put("borrowDate", borrowing.getBorrowDate());
        data.put("returnDate", borrowing.getReturnDate());
        data.put("returned", borrowing.isReturned());
        return data;
    }

    public Map<String, Object> getBorrowingReport(LocalDate fromDate, LocalDate toDate) {
        int totalBooks = borrowingRepository.countDistinctBookByBorrowDateBetween(fromDate, toDate);
        List<Borrowing> borrowings = borrowingRepository.findByBorrowDateBetween(fromDate, toDate);
        long totalUsers = borrowings.stream()
                .map(b -> b.getAccount().getId())
                .distinct()
                .count();

        Map<String, Long> borrowedBooks = borrowings.stream()
                .collect(Collectors.groupingBy(b -> b.getBook().getTitle(), Collectors.counting()));

        Map<String, Object> report = new HashMap<>();
        report.put("totalUsers", totalUsers);
        report.put("totalBooks", totalBooks);
        report.put("borrowedBooks", borrowedBooks);

        return report;
    }
}