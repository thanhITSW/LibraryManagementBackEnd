package nmtt.demo.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.service.BookService;
import nmtt.demo.service.CloudinaryService;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {
    BookService bookService;
    CloudinaryService cloudinaryService;

    @GetMapping
    ApiResponse<List<BookResponse>> getAllBooks() {

        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getAllBook())
                .build();
    }

    @PostMapping
    public ApiResponse<BookResponse> createBook(@RequestBody @Valid BookCreationRequest request){
        ApiResponse<BookResponse> apiResponse = new ApiResponse<>();

        apiResponse.setResult(bookService.createBook(request));
        return apiResponse;
    }

    @PutMapping("/{bookId}")
    public BookResponse updateBookById(@PathVariable("bookId") String bookId, @RequestBody BookUpdateRequest request){
        return bookService.updateBookById(bookId, request);
    }

    @DeleteMapping("/{bookId}")
    public ApiResponse<String> deleteBookById(@PathVariable("bookId") String bookId){
        bookService.deleteBookById(bookId);
        return ApiResponse.<String>builder().result("Book has been deleted").build();
    }

    @PostMapping("/search")
    public ApiResponse<List<BookResponse>> searchBook(@RequestParam String keyword){

        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.searchBooks(keyword))
                .build();

    }

    @PostMapping("/importCsv")
    public ApiResponse<String> importDataByCsv(@RequestParam("file") MultipartFile file){
        bookService.importBooksFromCsv(file);
        return ApiResponse.<String>builder().result("Add data successfully").build();
    }

    @PostMapping("/{bookId}/upload")
    public ApiResponse<?> uploadBookImage(@PathVariable String bookId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                return ApiResponse.<String>builder().result("File size must be under 2MB!").build();
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
                return ApiResponse.<String>builder().result("Only PNG and JPG images are allowed!").build();
            }

            Map uploadResult = cloudinaryService.uploadFile(file, "trainJava");

            BookResponse bookResponse = bookService.updateBookImage(bookId,
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id"));

            return ApiResponse.<BookResponse>builder()
                    .result(bookResponse)
                    .build();

        } catch (IOException e) {
            return ApiResponse.<String>builder().result("Error uploading file: " + e.getMessage()).build();
        }
    }

    @GetMapping("/{bookId}/preview")
    public ApiResponse<?> previewBookImage(@PathVariable String bookId) {
        String imageUrl = bookService.getBookImageUrl(bookId);
        if (imageUrl == null) {
            return ApiResponse.<String>builder().result("Image not found").build();
        }
        return ApiResponse.<String>builder().result(imageUrl).build();
    }

    @DeleteMapping("/{bookId}/delete-image")
    public ApiResponse<?> deleteBookImage(@PathVariable String bookId) {
        try {
            bookService.deleteBookImage(bookId);
            return ApiResponse.<String>builder().result("Book image deleted successfully!").build();
        } catch (Exception e) {
            return ApiResponse.<String>builder().result("Error deleting file: " + e.getMessage()).build();
        }
    }
}
