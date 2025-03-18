//package nmtt.demo.service;
//
//import nmtt.demo.entity.Account;
//import nmtt.demo.entity.Book;
//import nmtt.demo.entity.Borrowing;
//import nmtt.demo.enums.ErrorCode;
//import nmtt.demo.exception.AppException;
//import nmtt.demo.repository.AccountRepository;
//import nmtt.demo.repository.BookRepository;
//import nmtt.demo.repository.BorrowingRepository;
//import nmtt.demo.service.borrowing.BorrowingService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.TestPropertySource;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@TestPropertySource("/test.properties")
//public class BorrowingServiceTest {
//    @Autowired
//    private BorrowingService borrowingService;
//
//    @MockBean
//    private BorrowingRepository borrowingRepository;
//
//    @MockBean
//    private AccountRepository accountRepository;
//
//    @MockBean
//    private BookRepository bookRepository;
//
//    private Account account;
//    private Book book;
//
//    @BeforeEach
//    public void setUp() {
//        account = Account.builder()
//                .id("account-id")
//                .email("john@gmail.com")
//                .firstName("John")
//                .lastName("Doe")
//                .dob(LocalDate.of(1990, 1, 1))
//                .active(true)
//                .build();
//
//        book = Book.builder()
//                .id("book-id")
//                .title("Book Title")
//                .author("Author Name")
//                .category("Category")
//                .totalCopies(5)
//                .availableCopies(5)
//                .imageUrl("http://example.com/image.jpg")
//                .imagePublicId("image-public-id")
//                .build();
//    }
//
//    @Test
//    public void testBorrowBook_success() {
//        // Arrange
//        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
//        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
//        when(borrowingRepository.existsByAccountIdAndBookIdAndReturnedFalse(account.getId(), book.getId())).thenReturn(false);
//
//        // Act
//        borrowingService.borrowBook(account.getId(), book.getId());
//
//        // Assert
//        assertEquals(4, book.getAvailableCopies());
//        verify(borrowingRepository, times(1)).save(any(Borrowing.class));
//        verify(bookRepository, times(1)).save(book);
//    }
//
//    @Test
//    public void testBorrowBook_userNotExist() {
//        // Arrange
//        when(accountRepository.findById(account.getId())).thenReturn(Optional.empty());
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () -> borrowingService.borrowBook(account.getId(), book.getId()));
//        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//    }
//
//    @Test
//    public void testBorrowBook_bookNotExist() {
//        // Arrange
//        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
//        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () -> borrowingService.borrowBook(account.getId(), book.getId()));
//        assertEquals(ErrorCode.BOOK_NOT_EXISTED, exception.getErrorCode());
//    }
//
//    @Test
//    public void testBorrowBook_alreadyBorrowed() {
//        // Arrange
//        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
//        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
//        when(borrowingRepository.existsByAccountIdAndBookIdAndReturnedFalse(account.getId(), book.getId())).thenReturn(true);
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () -> borrowingService.borrowBook(account.getId(), book.getId()));
//        assertEquals(ErrorCode.BORROWED_BOOK, exception.getErrorCode());
//    }
//
//    @Test
//    public void testBorrowBook_notAvailable() {
//        // Arrange
//        book.setAvailableCopies(0);
//        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
//        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () -> borrowingService.borrowBook(account.getId(), book.getId()));
//        assertEquals(ErrorCode.NOT_AVAILABLE_BOOK, exception.getErrorCode());
//    }
//
//    @Test
//    public void testReturnBook_success() {
//        // Arrange
//        Borrowing borrowing = Borrowing.builder()
//                .account(account)
//                .book(book)
//                .borrowDate(LocalDate.now())
//                .returned(false)
//                .build();
//
//        when(borrowingRepository.findByAccountIdAndBookIdAndReturnedFalse(account.getId(), book.getId())).thenReturn(Optional.of(borrowing));
//        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
//
//        // Act
//        borrowingService.returnBook(account.getId(), book.getId());
//
//        // Assert
//        assertTrue(borrowing.isReturned());
//        assertNotNull(borrowing.getReturnDate());
//        assertEquals(6, book.getAvailableCopies());
//        verify(borrowingRepository, times(1)).save(borrowing);
//        verify(bookRepository, times(1)).save(book);
//    }
//
//    @Test
//    public void testReturnBook_borrowRecordNotFound() {
//        // Arrange
//        when(borrowingRepository.findByAccountIdAndBookIdAndReturnedFalse(account.getId(), book.getId())).thenReturn(Optional.empty());
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () -> borrowingService.returnBook(account.getId(), book.getId()));
//        assertEquals(ErrorCode.BORROW_RECORD_NOT_FOUND, exception.getErrorCode());
//    }
//
//    @Test
//    public void testGetBorrowedBooks() {
//        // Arrange
//        Borrowing borrowing = Borrowing.builder()
//                .account(account)
//                .book(book)
//                .borrowDate(LocalDate.now())
//                .returned(false)
//                .build();
//
//        when(borrowingRepository
//                .findByAccountIdAndReturnedFalse(account.getId()))
//                .thenReturn(List.of(borrowing));
//
//        // Act
//        var result = borrowingService.getBorrowedBooks(account.getId());
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(book.getId(), result.get(0).getId());
//    }
//}
