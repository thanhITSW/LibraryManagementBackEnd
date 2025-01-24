package nmtt.demo.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.entity.Book;
import nmtt.demo.exception.AppException;
import nmtt.demo.exception.ErrorCode;
import nmtt.demo.mapper.BookMapper;
import nmtt.demo.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookService {
    BookRepository bookRepository;
    BookMapper bookMapper;

    @Transactional
    public BookResponse createBook(BookCreationRequest request){
        if(bookRepository.existsByTitle(request.getTitle())){
            throw new AppException(ErrorCode.BOOK_EXISTED);
        }

        Book book = bookMapper.toBook(request);

        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    public List<BookResponse> getAllBook(){
        return bookRepository.findAll().stream()
                .map(bookMapper::toBookResponse).toList();
    }

    @Transactional
    public BookResponse updateBookById(String bookId, BookUpdateRequest request){
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookMapper.updateBook(book, request);

        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBookById(String bookId){
        bookRepository.deleteById(bookId);
    }

    public List<BookResponse> searchBooks(String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(lowerKeyword) ||
                        book.getAuthor().toLowerCase().contains(lowerKeyword) ||
                        book.getCategory().toLowerCase().contains(lowerKeyword))
                .map(bookMapper::toBookResponse)
                .toList();
    }

    @Transactional
    public void importBooksFromCsv(MultipartFile file) {

        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new AppException(ErrorCode.INVALID_CSV_FORMAT);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()
                , StandardCharsets.UTF_8))) {

            List<Book> books = new ArrayList<>();
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header line
                }
                String[] data = line.split(",");
                if (data.length < 5) {
                    throw new AppException(ErrorCode.INVALID_CSV_FORMAT);
                }

                Book book = new Book();
                book.setTitle(data[0].trim());
                book.setAuthor(data[1].trim());
                book.setCategory(data[2].trim());
                book.setTotalCopies(Integer.parseInt(data[3].trim()));
                book.setAvailableCopies(Integer.parseInt(data[4].trim()));
//                book.setAvailable(Boolean.parseBoolean(data[5].trim()));
                books.add(book);
            }
            bookRepository.saveAll(books);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CSV_IMPORT_FAILED);
        }
    }
}