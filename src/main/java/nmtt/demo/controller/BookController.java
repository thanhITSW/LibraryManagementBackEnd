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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/books")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {
    BookService bookService;

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
}
