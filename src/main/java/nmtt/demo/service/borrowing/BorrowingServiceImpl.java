package nmtt.demo.service.borrowing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Book.BookRequest;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.BookRepository;
import nmtt.demo.repository.BorrowingRepository;
import nmtt.demo.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingServiceImpl implements BorrowingService{
    private final BorrowingRepository borrowingRepository;
    private final AccountRepository accountRepository;
    private final BookRepository bookRepository;

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

        borrowingRepository.save(borrowing);
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

        //Update status book
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());
        borrowingRepository.save(borrowing);

        //Increase the number of books available
        Book book = borrowing.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }

    /**
     * Retrieves a list of books currently borrowed by an account.
     * This method fetches all borrowing records for the given account that have not been returned,
     * and returns the associated book details.
     *
     * @param accountId The ID of the account whose borrowed books are to be retrieved.
     * @return A list of books that are currently borrowed by the specified account.
     */
    @Transactional
    @Override
    public List<Book> getBorrowedBooks() {
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
        List<Borrowing> borrowings = borrowingRepository
                .findByAccountIdAndReturnedFalse(issuer);

        return borrowings.stream()
                .map(Borrowing::getBook)
                .collect(Collectors.toList());
    }
}