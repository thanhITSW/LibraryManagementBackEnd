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

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBook();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable String bookId) {
        BookResponse book = bookService.getBookById(bookId);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(BookCriteria criteria
            , Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.findByCriteria(criteria, pageable));
    }
}
