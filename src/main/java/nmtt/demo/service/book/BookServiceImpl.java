package nmtt.demo.service.book;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.entity.Book;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.mapper.BookMapper;
import nmtt.demo.repository.BookRepository;
import nmtt.demo.service.activity_log.ActivityLogService;
import nmtt.demo.service.cloudinary.CloudinaryService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService{
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CloudinaryService cloudinaryService;
    private final ActivityLogService logService;

    /**
     * Creates a new book with the provided details.
     *
     * @param request The request containing the details of the book to be created.
     * @return A response containing the details of the created book.
     * @throws AppException If a book with the same title already exists.
     */
    @Transactional
    @Override
    public BookResponse createBook(BookCreationRequest request){
        if(bookRepository.existsByTitle(request.getTitle())){
            throw new AppException(ErrorCode.BOOK_EXISTED);
        }

        Book book = bookMapper.toBook(request);
        Book savedBook = bookRepository.save(book);

        HashMap<String, Object> newData = toMap(savedBook);
        logService.log("CREATE", "BOOK", savedBook.getId(),
                "Admin created a new book", null, newData);

        return bookMapper.toBookResponse(savedBook);
    }

    /**
     * Retrieves all books from the repository.
     *
     * @return A list of responses containing details of all books.
     */
    @Override
    public List<BookResponse> getAllBook(){
        return bookRepository.findAll().stream()
                .map(bookMapper::toBookResponse).toList();
    }

    /**
     * Retrieves a book by its ID from the repository.
     *
     * @param id The unique identifier of the book to be retrieved.
     * @return A {@link BookResponse} object containing the details of the book if found.
     *         If the book is not found, a {@link RuntimeException} is thrown with the message "Book not found".
     */
    @Override
    public BookResponse getBookById(String id){
        return bookRepository.findById(id)
               .map(bookMapper::toBookResponse)
               .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    /**
     * Updates the details of a book by its ID.
     *
     * @param bookId The ID of the book to be updated.
     * @param request The request containing the updated details of the book.
     * @return A response containing the updated details of the book.
     * @throws RuntimeException If the book with the given ID is not found.
     */
    @Transactional
    @Override
    public BookResponse updateBookById(String bookId, BookUpdateRequest request){
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        HashMap<String, Object> oldData = toMap(book);

        bookMapper.updateBook(book, request);
        Book updatedBook = bookRepository.save(book);

        HashMap<String, Object> newData = toMap(updatedBook);
        logService.log("UPDATE", "BOOK", bookId,
                "Admin updated book", oldData, newData);

        return bookMapper.toBookResponse(updatedBook);
    }

    /**
     * Deletes a book by its ID, including its associated image.
     *
     * @param bookId The ID of the book to be deleted.
     */
    @Transactional
    @Override
    public void deleteBookById(String bookId){
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        HashMap<String, Object> oldData = toMap(book);

        deleteBookImage(bookId);
        bookRepository.deleteById(bookId);

        logService.log("DELETE", "BOOK", bookId,
                "Admin deleted book", oldData, null);
    }

    /**
     * Searches for books based on the given keyword.
     * <p>
     * This method uses Spring Data JPA Specifications to find books where the title,
     * author, or category contains the keyword (case-insensitive).
     * </p>
     *
     * @param keyword The search keyword.
     * @return A list of books matching the search criteria, mapped to {@link BookResponse}.
     */
    @Override
    public List<BookResponse> searchBooks(String keyword) {
        Specification<Book> spec = BookSpecification.searchByKeyword(keyword);
        return bookRepository.findAll(spec)
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }


    /**
     * Imports books from a CSV file. The method reads the file, processes each line (skipping the header),
     * and saves the book data into the repository. The CSV file must contain at least 5 columns: title, author,
     * category, totalCopies, and availableCopies.
     *
     * @param file The CSV file containing the book data.
     * @throws AppException If the file is not in CSV format, the data is invalid, or the import fails.
     */
    @Transactional
    @Override
    public void importBooksFromCsv(MultipartFile file) {
        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new AppException(ErrorCode.INVALID_CSV_FORMAT);
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            List<Book> books = new ArrayList<>();
            List<String[]> allRows = csvReader.readAll();

            if (allRows.isEmpty()) {
                throw new AppException(ErrorCode.EMPTY_CSV_FILE);
            }

            String[] headerRow = allRows.get(0);
            List<String> expectedHeaders = List.of("title", "author", "category", "totalCopies", "availableCopies");
            List<String> actualHeaders = Arrays.stream(headerRow)
                    .map(h -> h.replace("\uFEFF", "").trim())
                    .toList();

            if (!actualHeaders.equals(expectedHeaders)) {
                throw new AppException(ErrorCode.INVALID_CSV_DATA);
            }

            List<Book> existingBooks = bookRepository.findAll();
            Map<String, Book> existingBooksMap = existingBooks.stream()
                    .collect(Collectors.toMap(
                            book -> (book.getTitle() + "|" + book.getAuthor()).toLowerCase(),
                            book -> book,
                            (b1, b2) -> b1
                    ));

            for (int i = 1; i < allRows.size(); i++) {
                String[] data = allRows.get(i);

                if (data.length < 5) {
                    throw new AppException(ErrorCode.INVALID_CSV_FORMAT);
                }

                try {
                    String title = data[0].trim();
                    String author = data[1].trim();
                    String key = (title + "|" + author).toLowerCase();

                    if (existingBooksMap.containsKey(key)) {
                        Book existingBook = existingBooksMap.get(key);
                        existingBook.setTotalCopies(existingBook.getTotalCopies() + Integer.parseInt(data[3].trim()));
                        existingBook.setAvailableCopies(existingBook.getAvailableCopies() + Integer.parseInt(data[4].trim()));
                        books.add(existingBook);
                        continue;
                    }

                    Book book = new Book();
                    book.setTitle(title);
                    book.setAuthor(author);
                    book.setCategory(data[2].trim());
                    book.setTotalCopies(Integer.parseInt(data[3].trim()));
                    book.setAvailableCopies(Integer.parseInt(data[4].trim()));
                    books.add(book);
                } catch (NumberFormatException e) {
                    throw new AppException(ErrorCode.INVALID_CSV_DATA);
                }
            }

            if (!books.isEmpty()) {
                bookRepository.saveAll(books);
            }
        } catch (IOException | CsvException e) {
            throw new AppException(ErrorCode.CSV_IMPORT_FAILED);
        }
    }

    /**
     * Updates the image of a book. The method first checks if the book has an existing image.
     * If so, it deletes the image from Cloudinary. Then, it updates the book's image URL and public ID in the database.
     *
     * @param bookId The ID of the book whose image needs to be updated.
     * @param imageUrl The new image URL.
     * @param publicId The new image public ID from Cloudinary.
     * @return The updated book response with the new image information.
     * @throws RuntimeException If the book is not found, or the image deletion from Cloudinary fails.
     */
    @Transactional
    @Override
    public BookResponse updateBookImage(String bookId, String imageUrl, String publicId) {
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        HashMap<String, Object> oldData = toMap(book);

        // delete image on Cloudinary if have
        if (book.getImagePublicId() != null) {
            try {
                cloudinaryService.deleteFile(book.getImagePublicId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete old image: " + e.getMessage());
            }
        }

        // update image in database
        book.setImageUrl(imageUrl);
        book.setImagePublicId(publicId);
        Book updatedBook = bookRepository.save(book);

        HashMap<String, Object> newData = toMap(updatedBook);
        logService.log("UPDATE", "BOOK", bookId,
                "Admin updated book image", oldData, newData);

        return bookMapper.toBookResponse(updatedBook);
    }

    /**
     * Retrieves the image URL of a book by its ID.
     *
     * @param bookId The ID of the book.
     * @return The image URL of the book if found, otherwise null.
     */
    @Override
    public String getBookImageUrl(String bookId) {
        Book book = bookRepository
                .findById(bookId)
                .orElse(null);

        return book != null ? book.getImageUrl() : null;
    }

    /**
     * Deletes the image associated with a book by its ID.
     * The image is removed from the Cloudinary service and the image data is cleared from the database.
     *
     * @param bookId The ID of the book whose image is to be deleted.
     * @throws RuntimeException if the book is not found or the image deletion fails.
     */
    @Transactional
    @Override
    public void deleteBookImage(String bookId) {
        Book book = bookRepository
                .findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Delete image on Cloudinary
        if (book.getImagePublicId() != null) {
            try {
                cloudinaryService.deleteFile(book.getImagePublicId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete image: " + e.getMessage());
            }
        }

        // Delete data in DB
        book.setImageUrl(null);
        book.setImagePublicId(null);
        bookRepository.save(book);
    }

    /**
     * Converts a Book entity into a HashMap containing its properties.
     *
     * @param book The Book entity to be converted.
     * @return A HashMap containing the properties of the Book entity.
     *         The keys of the HashMap are the property names, and the values are the corresponding property values.
     */
    private HashMap<String, Object> toMap(Book book) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", book.getId());
        data.put("title", book.getTitle());
        data.put("author", book.getAuthor());
        data.put("category", book.getCategory());
        data.put("totalCopies", book.getTotalCopies());
        data.put("availableCopies", book.getAvailableCopies());
        data.put("imageUrl", book.getImageUrl());
        data.put("imagePublicId", book.getImagePublicId());
        data.put("available", book.isAvailable());
        return data;
    }
}