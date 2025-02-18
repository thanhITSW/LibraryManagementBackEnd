package nmtt.demo.service;

import nmtt.demo.dto.request.Book.BookCreationRequest;
import nmtt.demo.dto.request.Book.BookUpdateRequest;
import nmtt.demo.dto.response.Book.BookResponse;
import nmtt.demo.entity.Book;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.mapper.BookMapper;
import nmtt.demo.repository.BookRepository;
import nmtt.demo.service.book.BookService;
import nmtt.demo.service.book.BookSpecification;
import nmtt.demo.service.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class BookServiceTest {
    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private BookMapper bookMapper;

    @Autowired
    private BookService bookService;

    @MockBean
    private CloudinaryService cloudinaryService;

    private BookCreationRequest request;
    private BookUpdateRequest updateRequest;
    private Book book;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        request = new BookCreationRequest();
        request.setTitle("Test Book");
        request.setAuthor("Test Author");
        request.setCategory("Test Category");
        request.setTotalCopies(10);
        request.setAvailableCopies(10);

        updateRequest = new BookUpdateRequest();
        updateRequest.setTitle("Updated Book");
        updateRequest.setAuthor("Updated Author");
        updateRequest.setCategory("Updated Category");
        updateRequest.setTotalCopies(5);
        updateRequest.setAvailableCopies(3);
        updateRequest.setAvailable(true);

        book = Book.builder()
                .id("1")
                .title("Test Book")
                .author("Test Author")
                .category("Test Category")
                .totalCopies(10)
                .availableCopies(10)
                .imageUrl("old_image.jpg")
                .imagePublicId("old_image_public_id")
                .available(true)
                .build();

        bookResponse = new BookResponse();
        bookResponse.setTitle("Test Book");
        bookResponse.setAuthor("Test Author");
        bookResponse.setCategory("Test Category");
        bookResponse.setTotalCopies(10);
        bookResponse.setAvailableCopies(10);
        bookResponse.setAvailable(true);
    }

    @Test
    void createBook_ShouldThrowException_WhenBookAlreadyExists() {
        when(bookRepository.existsByTitle(request.getTitle())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> bookService.createBook(request));
        assertEquals(ErrorCode.BOOK_EXISTED, exception.getErrorCode());
    }

    @Test
    void createBook_ShouldSaveAndReturnBookResponse_WhenBookDoesNotExist() {
        when(bookRepository.existsByTitle(request.getTitle())).thenReturn(false);
        when(bookMapper.toBook(request)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.createBook(request);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        assertEquals("Test Category", result.getCategory());
        assertEquals(10, result.getTotalCopies());
        assertEquals(10, result.getAvailableCopies());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void getAllBook_ShouldReturnListOfBookResponses() {
        List<Book> books = Arrays.asList(book);
        List<BookResponse> bookResponses = Arrays.asList(bookResponse);

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toBookResponse(book)).thenReturn(bookResponse);

        List<BookResponse> result = bookService.getAllBook();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void updateBookById_ShouldThrowException_WhenBookNotFound() {
        when(bookRepository.findById("invalid-id")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookService.updateBookById("invalid-id", updateRequest));
        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void updateBookById_ShouldUpdateAndReturnBookResponse_WhenBookExists() {
        when(bookRepository.findById("valid-id")).thenReturn(Optional.of(book));
        doNothing().when(bookMapper).updateBook(book, updateRequest);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toBookResponse(book)).thenReturn(bookResponse);

        BookResponse result = bookService.updateBookById("valid-id", updateRequest);

        assertNotNull(result);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void deleteBookById_ShouldDeleteBook_WhenBookExists() {
        when(bookRepository.findById("valid-id")).thenReturn(Optional.of(book));
        bookService.deleteBookById("valid-id");
        verify(bookRepository, times(1)).deleteById("valid-id");
    }

    @Test
    void importBooksFromCsv_ShouldThrowException_WhenFileIsNotCsv() {
        MockMultipartFile file = new MockMultipartFile("file",
                "books.txt",
                "text/plain",
                "invalid content".getBytes());

        AppException exception = assertThrows(AppException.class, () -> bookService.importBooksFromCsv(file));
        assertEquals(ErrorCode.INVALID_CSV_FORMAT, exception.getErrorCode());
    }

    @Test
    void importBooksFromCsv_ShouldImportBooks_WhenValidCsvFile() throws Exception {
        String csvContent = "title,author,category,totalCopies,availableCopies\n"
                + "Book 1,Author 1,Category 1,10,5\n"
                + "Book 2,Author 2,Category 2,15,10\n";
        MockMultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", csvContent.getBytes());

        bookService.importBooksFromCsv(file);

        ArgumentCaptor<List<Book>> bookCaptor = ArgumentCaptor.forClass(List.class);
        verify(bookRepository, times(1)).saveAll(bookCaptor.capture());
        List<Book> savedBooks = bookCaptor.getValue();

        assertEquals(2, savedBooks.size());
        assertEquals("Book 1", savedBooks.get(0).getTitle());
        assertEquals("Book 2", savedBooks.get(1).getTitle());
    }

    @Test
    void importBooksFromCsv_ShouldThrowException_WhenCsvHasInvalidFormat() {
        String csvContent = "title,author,category,totalCopies\n"  // Thiếu availableCopies
                + "Book 1,Author 1,Category 1,10\n";
        MockMultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", csvContent.getBytes());

        AppException exception = assertThrows(AppException.class, () -> bookService.importBooksFromCsv(file));
        assertEquals(ErrorCode.CSV_IMPORT_FAILED, exception.getErrorCode());
    }

    @Test
    void importBooksFromCsv_ShouldThrowException_WhenFileCannotBeRead() throws Exception {
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("books.csv");
        when(file.getInputStream()).thenThrow(new RuntimeException("Read error"));

        AppException exception = assertThrows(AppException.class, () -> bookService.importBooksFromCsv(file));
        assertEquals(ErrorCode.CSV_IMPORT_FAILED, exception.getErrorCode());
    }

    @Test
    void searchBooks_shouldReturnBookResponseList_whenKeywordIsGiven() {
        // Arrange
        String keyword = "Java";

        // Creating Book entities with all the fields
        Book book1 = Book.builder()
                .id("1")
                .title("Java Programming")
                .author("Author1")
                .category("Programming")
                .totalCopies(10)
                .availableCopies(5)
                .available(true)
                .imageUrl("image1.jpg")
                .imagePublicId("imagePublicId1")
                .build();

        Book book2 = Book.builder()
                .id("2")
                .title("Advanced Java")
                .author("Author2")
                .category("Programming")
                .totalCopies(8)
                .availableCopies(4)
                .available(true)
                .imageUrl("image2.jpg")
                .imagePublicId("imagePublicId2")
                .build();

        BookResponse bookResponse1 = BookResponse.builder()
                .id("1")
                .title("Java Programming")
                .author("Author1")
                .category("Programming")
                .totalCopies(10)
                .availableCopies(5)
                .available(true)
                .build();

        BookResponse bookResponse2 = BookResponse.builder()
                .id("2")
                .title("Advanced Java")
                .author("Author2")
                .category("Programming")
                .totalCopies(8)
                .availableCopies(4)
                .available(true)
                .build();

        when(bookRepository.findAll(any(Specification.class))).thenReturn(List.of(book1, book2));

        when(bookMapper.toBookResponse(book1)).thenReturn(bookResponse1);
        when(bookMapper.toBookResponse(book2)).thenReturn(bookResponse2);

        List<BookResponse> result = bookService.searchBooks(keyword);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
        assertEquals("Advanced Java", result.get(1).getTitle());

        verify(bookRepository, times(1)).findAll(any(Specification.class));
        verify(bookMapper, times(1)).toBookResponse(book1);
        verify(bookMapper, times(1)).toBookResponse(book2);
    }

    @Test
    void testUpdateBookImage_Success() throws IOException {
        String newImageUrl = "new_image.jpg";
        String newPublicId = "new_image_public_id";

        when(bookRepository.findById("1")).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(bookResponse);

        BookResponse response = bookService.updateBookImage("1", newImageUrl, newPublicId);

        assertNotNull(response);
        verify(cloudinaryService, times(1)).deleteFile("old_image_public_id");
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    void testUpdateBookImage_BookNotFound() {
        when(bookRepository.findById("2")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                bookService.updateBookImage("2", "new_image.jpg", "new_image_public_id"));

        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void testUpdateBookImage_CloudinaryDeleteFailure() throws IOException {
        doThrow(new RuntimeException("Cloudinary deletion failed"))
                .when(cloudinaryService).deleteFile("old_image_public_id");

        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        Exception exception = assertThrows(RuntimeException.class, () ->
                bookService.updateBookImage("1", "new_image.jpg", "new_image_public_id"));

        assertEquals("Failed to delete old image: Cloudinary deletion failed", exception.getMessage());
    }

    @Test
    void testGetBookImageUrl_BookExists() {
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));
        String imageUrl = bookService.getBookImageUrl("1");
        assertEquals("old_image.jpg", imageUrl);
    }

    @Test
    void testGetBookImageUrl_BookNotFound() {
        when(bookRepository.findById("2")).thenReturn(Optional.empty());
        String imageUrl = bookService.getBookImageUrl("2");
        assertNull(imageUrl);
    }

    @Test
    void testDeleteBookImage_Success() throws IOException {
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));

        bookService.deleteBookImage("1");

        verify(cloudinaryService, times(1)).deleteFile("old_image_public_id");
        verify(bookRepository, times(1)).save(any(Book.class));
        assertNull(book.getImageUrl());
        assertNull(book.getImagePublicId());
    }

    @Test
    void testDeleteBookImage_BookNotFound() {
        when(bookRepository.findById("2")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                bookService.deleteBookImage("2"));

        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void testDeleteBookImage_CloudinaryDeleteFailure() throws IOException {
        when(bookRepository.findById("1")).thenReturn(Optional.of(book));
        doThrow(new RuntimeException("Cloudinary deletion failed"))
                .when(cloudinaryService).deleteFile("old_image_public_id");

        Exception exception = assertThrows(RuntimeException.class, () ->
                bookService.deleteBookImage("1"));

        assertEquals("Failed to delete image: Cloudinary deletion failed", exception.getMessage());
    }
}
