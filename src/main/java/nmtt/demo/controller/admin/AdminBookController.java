package nmtt.demo.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.entity.Book;
import nmtt.demo.service.book.BookService;
import nmtt.demo.service.cloudinary.CloudinaryService;
import nmtt.demo.service.search.book.BookCriteria;
import nmtt.demo.service.search.book.BookQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${admin-mapping}/books")
@Slf4j
@RequiredArgsConstructor
public class AdminBookController {
    private final BookService bookService;
    private final CloudinaryService cloudinaryService;
    private final BookQueryService bookQueryService;

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBook();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@RequestBody @Valid BookCreationRequest request) {
        BookResponse bookResponse = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookResponse);
    }


    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBookById(
            @PathVariable("bookId") String bookId,
            @RequestBody @Valid BookUpdateRequest request) {

        BookResponse updatedBook = bookService.updateBookById(bookId, request);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> deleteBookById(@PathVariable("bookId") String bookId) {
        bookService.deleteBookById(bookId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Delete book successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBook(BookCriteria criteria
            , Pageable pageable) {

        return ResponseEntity.ok(bookQueryService.findByCriteria(criteria, pageable));
    }

    @PostMapping("/import-csv")
    public ResponseEntity<ApiResponse<String>> importDataByCsv(@RequestParam("file") MultipartFile file) {
        bookService.importBooksFromCsv(file);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Add data successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookId}/upload")
    public ResponseEntity<?> uploadBookImage(@PathVariable String bookId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                ApiResponse<String> response = ApiResponse.<String>builder()
                        .message("File size must be under 2MB!")
                        .code(9999)
                        .build();
                return ResponseEntity.badRequest().body(response);
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
                ApiResponse<String> response = ApiResponse.<String>builder()
                        .message("Only PNG and JPG images are allowed!")
                        .code(9999)
                        .build();
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "trainJava");

            BookResponse bookResponse = bookService.updateBookImage(bookId,
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"));

            return ResponseEntity.ok(bookResponse);

        } catch (IOException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("Error uploading file: " + e.getMessage())
                    .code(9999)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @GetMapping("/{bookId}/preview")
    public ResponseEntity<?> previewBookImage(@PathVariable String bookId) {
        String imageUrl = bookService.getBookImageUrl(bookId);

        if (imageUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Image not found"));
        }

        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/{bookId}/delete-image")
    public ResponseEntity<?> deleteBookImage(@PathVariable String bookId) {
        try {
            bookService.deleteBookImage(bookId);
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("Book image deleted successfully!")
                    .code(9999)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("Error deleting file: " + e.getMessage())
                    .code(9999)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}