package nmtt.demo.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.request.Book.BookRequest;
import nmtt.demo.dto.response.Book.BorrowBookResponse;
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

    /**
     * Handles the borrowing of books for a user.
     *
     * @param request The request containing the book details to be borrowed.
     * @return A ResponseEntity containing an ApiResponse with a success message if the book is borrowed successfully,
     *         or an error message if an exception occurs.
     * @throws IllegalArgumentException If the book is not available or the user has reached the maximum borrowing limit.
     */
    @PostMapping("/borrow")
    public ResponseEntity<ApiResponse<String>> borrowBook(@RequestBody BookRequest request) {
        try {
            borrowingService.borrowBook(request);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("Book borrowed successfully!")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .code(9999)
                    .message("error" +  e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Handles the borrowing of books for a user.
     *
     * @param request The request containing the book details to be borrowed.
     * @return A ResponseEntity containing an ApiResponse with a success message if the book is borrowed successfully,
     *         or an error message if an exception occurs.
     * @throws IllegalArgumentException If the book is not available or the user has reached the maximum borrowing limit.
     */
    @PostMapping("/return")
    public ResponseEntity<ApiResponse<String>> returnBook(@RequestBody BookRequest request) {
        try {
            borrowingService.returnBook(request);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("Book returned successfully!")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("error" + e.getMessage())
                    .code(9999)
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Retrieves a list of borrowed books for the current user.
     *
     * @return A ResponseEntity containing a list of borrowed books if found,
     *         or an ApiResponse with a message indicating no borrowed books found if the list is empty.
     */
    @GetMapping("/borrowed-books")
    public ResponseEntity<?> getBorrowedBooks() {
        List<BorrowBookResponse> borrowedBooks = borrowingService.getBorrowedBooks();

        if (borrowedBooks.isEmpty()) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("No borrowed books found.")
                    .code(1001)
                    .build();
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(borrowedBooks);
    }

    /**
     * Handles the search functionality for books based on the provided criteria and pagination.
     *
     * @param criteria The search criteria for filtering books.
     * @param pageable The pagination details for retrieving a specific page of results.
     * @return A ResponseEntity containing a Page of Book entities that match the search criteria.
     *         If no books are found, an empty Page is returned.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(@ParameterObject BookCriteria criteria
            ,@ParameterObject Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.findByCriteria(criteria, pageable));
    }
}
