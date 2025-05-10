package nmtt.demo.controller.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.entity.Book;
import nmtt.demo.service.book.BookService;
import nmtt.demo.service.search.book.BookCriteria;
import nmtt.demo.service.search.book.BookQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${common-mapping}/books")
@Slf4j
@RequiredArgsConstructor
public class CommonBookController {
    private final BookService bookService;
    private final BookQueryService bookQueryService;

    /**
     * Retrieves all books from the system.
     *
     * @return a ResponseEntity containing a list of BookResponse objects.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBook();
        return ResponseEntity.ok(books);
    }

    /**
     * Retrieves a book by its unique identifier.
     *
     * @param bookId the unique identifier of the book to retrieve.
     * @return a ResponseEntity containing a BookResponse object.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     *         If the book with the given ID does not exist, the HTTP status code is 404 (Not Found).
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable String bookId) {
        BookResponse book = bookService.getBookById(bookId);
        return ResponseEntity.ok(book);
    }

    /**
     * Performs a search operation on books based on the provided criteria and pagination parameters.
     *
     * @param criteria an object encapsulating the search criteria (e.g., title, author, publication date).
     *                 This object should be created using the BookCriteria class.
     * @param pageable an object representing the pagination parameters (e.g., page number, page size, sorting).
     *                 This object should be created using Spring Data's Pageable interface.
     * @return a ResponseEntity containing a Page object of Book entities.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     *         The Page object contains the search results and pagination metadata.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(BookCriteria criteria
            , Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.findByCriteria(criteria, pageable));
    }
}
