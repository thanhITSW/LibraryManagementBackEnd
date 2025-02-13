package nmtt.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Book;
import nmtt.demo.service.borrowing.BorrowingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/borrowing")
@Slf4j
@RequiredArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;

    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestParam String accountId, @RequestParam String bookId) {
        try {
            borrowingService.borrowBook(accountId, bookId);
            return ResponseEntity.ok(Map.of("message", "Book borrowed successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnBook(@RequestParam String accountId, @RequestParam String bookId) {
        try {
            borrowingService.returnBook(accountId, bookId);
            return ResponseEntity.ok(Map.of("message", "Book returned successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/borrowed-books")
    public ResponseEntity<?> getBorrowedBooks(@PathVariable String accountId) {
        List<Book> borrowedBooks = borrowingService.getBorrowedBooks(accountId);

        if (borrowedBooks.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No borrowed books found."));
        }

        return ResponseEntity.ok(borrowedBooks);
    }

}
