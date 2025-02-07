package nmtt.demo.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.exception.AppException;
import nmtt.demo.exception.ErrorCode;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.BookRepository;
import nmtt.demo.repository.BorrowingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BorrowingService {
    BorrowingRepository borrowingRepository;
    AccountRepository accountRepository;
    BookRepository bookRepository;

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void borrowBook(String accountId, String bookId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Kiểm tra người dùng đã mượn loại sách này chưa
        boolean alreadyBorrowed = borrowingRepository.existsByAccountIdAndBookIdAndReturnedFalse(accountId, bookId);

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

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void returnBook(String accountId, String bookId){
        Borrowing borrowing = borrowingRepository.findByAccountIdAndBookIdAndReturnedFalse(accountId, bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_RECORD_NOT_FOUND));

        //Update trạng thái sách
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());
        borrowingRepository.save(borrowing);

        //Tăng số lượng sách khả dụng
        Book book = borrowing.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }
}
