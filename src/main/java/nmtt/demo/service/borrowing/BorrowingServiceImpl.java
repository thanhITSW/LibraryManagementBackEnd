package nmtt.demo.service.borrowing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.BookRepository;
import nmtt.demo.repository.BorrowingRepository;
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
     * Allows an account to borrow a book by its ID.
     * This method checks if the user has already borrowed the book, ensures the book is available,
     * and updates the book's available copies and borrowing records accordingly.
     *
     * @param accountId The ID of the account attempting to borrow the book.
     * @param bookId The ID of the book being borrowed.
     * @throws AppException if the user does not exist, the book does not exist, the user has already borrowed the book,
     *                      or the book is not available.
     */
    @Transactional
    @Override
    public void borrowBook(String accountId, String bookId){
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Check if the user has borrowed this type of book yet
        boolean alreadyBorrowed = borrowingRepository
                .existsByAccountIdAndBookIdAndReturnedFalse(accountId, bookId);

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
     * Allows an account to return a borrowed book by its ID.
     * This method updates the borrowing record to mark the book as returned,
     * updates the book's available copies, and saves the changes.
     *
     * @param accountId The ID of the account returning the book.
     * @param bookId The ID of the book being returned.
     * @throws AppException if the borrowing record is not found or the book has not been borrowed.
     */
    @Transactional
    @Override
    public void returnBook(String accountId, String bookId){
        Borrowing borrowing = borrowingRepository
                .findByAccountIdAndBookIdAndReturnedFalse(accountId, bookId)
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
    public List<Book> getBorrowedBooks(String accountId) {
        List<Borrowing> borrowings = borrowingRepository
                .findByAccountIdAndReturnedFalse(accountId);

        return borrowings.stream()
                .map(Borrowing::getBook)
                .collect(Collectors.toList());
    }
}