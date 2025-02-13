package nmtt.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.service.book.BookService;
import nmtt.demo.service.cloudinary.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
@Slf4j
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final CloudinaryService cloudinaryService;

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
    public ResponseEntity<Void> deleteBookById(@PathVariable("bookId") String bookId) {
        bookService.deleteBookById(bookId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PostMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBook(@RequestParam String keyword) {
        List<BookResponse> books = bookService.searchBooks(keyword);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/import-csv")
    public ResponseEntity<Map<String, String>> importDataByCsv(@RequestParam("file") MultipartFile file) {
        bookService.importBooksFromCsv(file);
        return ResponseEntity.ok(Map.of("message", "Add data successfully"));
    }

    @PostMapping("/{bookId}/upload")
    public ResponseEntity<?> uploadBookImage(@PathVariable String bookId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File size must be under 2MB!"));
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PNG and JPG images are allowed!"));
            }

            Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "trainJava");

            BookResponse bookResponse = bookService.updateBookImage(bookId,
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"));

            return ResponseEntity.ok(bookResponse);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
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
            return ResponseEntity.ok(Map.of("message", "Book image deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting file: " + e.getMessage()));
        }
    }
}
