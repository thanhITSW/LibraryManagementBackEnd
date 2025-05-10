package nmtt.demo.service.book;

import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {

    public BookResponse createBook(BookCreationRequest request);

    public List<BookResponse> getAllBook();
    public BookResponse getBookById(String id);

    public BookResponse updateBookById(String bookId, BookUpdateRequest request);

    public void deleteBookById(String bookId);

    public List<BookResponse> searchBooks(String keyword);

    public void importBooksFromCsv(MultipartFile file);

    public BookResponse updateBookImage(String bookId, String imageUrl, String publicId);
    public String getBookImageUrl(String bookId);
    public void deleteBookImage(String bookId);
}