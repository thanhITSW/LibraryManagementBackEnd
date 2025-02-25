package nmtt.demo.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Book.BookRequest;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.service.borrowing.BorrowingService;
import nmtt.demo.service.search.account.AccountCriteria;
import nmtt.demo.service.search.account.AccountQueryService;
import nmtt.demo.service.search.book.BookCriteria;
import nmtt.demo.service.search.book.BookQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${user-mapping}/borrowing")
@Slf4j
@RequiredArgsConstructor
public class UserBorrowingController {
    private final BorrowingService borrowingService;
    private final BookQueryService bookQueryService;

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody BookRequest request) {
        try {
            borrowingService.borrowBook(request);
            return ResponseEntity.ok(Map.of("message", "Book borrowed successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestBody BookRequest request) {
        try {
            borrowingService.returnBook(request);
            return ResponseEntity.ok(Map.of("message", "Book returned successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/borrowed-books")
    public ResponseEntity<?> getBorrowedBooks() {
        List<Book> borrowedBooks = borrowingService.getBorrowedBooks();

        if (borrowedBooks.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No borrowed books found."));
        }

        return ResponseEntity.ok(borrowedBooks);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(BookCriteria criteria
            , Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.findByCriteria(criteria, pageable));
    }

}
