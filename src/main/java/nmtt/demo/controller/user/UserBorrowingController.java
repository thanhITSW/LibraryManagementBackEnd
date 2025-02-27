package nmtt.demo.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.request.Book.BookRequest;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.service.borrowing.BorrowingService;
import nmtt.demo.service.search.account.AccountCriteria;
import nmtt.demo.service.search.account.AccountQueryService;
import nmtt.demo.service.search.book.BookCriteria;
import nmtt.demo.service.search.book.BookQueryService;
import org.springdoc.core.annotations.ParameterObject;
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
    public ResponseEntity<ApiResponse<String>> borrowBook(@RequestBody BookRequest request) {
        try {
            borrowingService.borrowBook(request);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .result("Book borrowed successfully!")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(9999)
                    .result("error" +  e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<String>> returnBook(@RequestBody BookRequest request) {
        try {
            borrowingService.returnBook(request);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .result("Book returned successfully!")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .result("error" + e.getMessage())
                    .code(9999)
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/borrowed-books")
    public ResponseEntity<?> getBorrowedBooks() {
        List<Book> borrowedBooks = borrowingService.getBorrowedBooks();

        if (borrowedBooks.isEmpty()) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .result("No borrowed books found.")
                    .code(1001)
                    .build();
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(borrowedBooks);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(@ParameterObject BookCriteria criteria
            ,@ParameterObject Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.findByCriteria(criteria, pageable));
    }
}
