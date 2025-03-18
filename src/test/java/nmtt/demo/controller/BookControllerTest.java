//package nmtt.demo.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import nmtt.demo.dto.request.Book.BookCreationRequest;
//import nmtt.demo.dto.request.Book.BookUpdateRequest;
//import nmtt.demo.dto.response.Book.BookResponse;
//import nmtt.demo.service.book.BookService;
//import nmtt.demo.service.cloudinary.CloudinaryService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestPropertySource("/test.properties")
//public class BookControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private BookService bookService;
//
//    @MockBean
//    private CloudinaryService cloudinaryService;
//
//    private BookCreationRequest bookCreationRequest;
//    private BookUpdateRequest bookUpdateRequest;
//    private BookResponse bookResponse;
//
//    private final String UPLOAD_URL = "/books/1/upload";
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//
//        bookCreationRequest = new BookCreationRequest();
//        bookCreationRequest.setTitle("Test Title");
//        bookCreationRequest.setAuthor("Test Author");
//        bookCreationRequest.setCategory("Test Category");
//        bookCreationRequest.setTotalCopies(10);
//        bookCreationRequest.setAvailableCopies(5);
//
//        bookUpdateRequest = new BookUpdateRequest();
//        bookUpdateRequest.setTitle("Updated Title");
//        bookUpdateRequest.setAuthor("Updated Author");
//        bookUpdateRequest.setCategory("Updated Category");
//        bookUpdateRequest.setTotalCopies(15);
//        bookUpdateRequest.setAvailableCopies(10);
//        bookUpdateRequest.setAvailable(true);
//
//        bookResponse = new BookResponse("1", "Test Title", "Test Author", "Test Category", 10, 5, true);
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testGetAllBooks() throws Exception {
//        List<BookResponse> books = Collections.singletonList(new BookResponse());
//        when(bookService.getAllBook()).thenReturn(books);
//
//        mockMvc.perform(get("/books"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testCreateBook() throws Exception {
//        when(bookService.createBook(any(BookCreationRequest.class))).thenReturn(bookResponse);
//
//        mockMvc.perform(post("/books")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookCreationRequest)))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testUpdateBookById() throws Exception {
//        when(bookService.updateBookById(anyString(), any())).thenReturn(bookResponse);
//
//        mockMvc.perform(put("/books/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookUpdateRequest)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testDeleteBookById() throws Exception {
//        doNothing().when(bookService).deleteBookById(anyString());
//
//        mockMvc.perform(delete("/books/1"))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testSearchBook() throws Exception {
//        List<BookResponse> books = Collections.singletonList(new BookResponse());
//        when(bookService.searchBooks(anyString())).thenReturn(books);
//
//        mockMvc.perform(post("/books/search").param("keyword", "test"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testUploadBookImage_Success() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "test.jpg", "image/jpeg", "test image".getBytes());
//        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(Map.of("secure_url", "url", "public_id", "id"));
//        when(bookService.updateBookImage(anyString(), anyString(), anyString())).thenReturn(new BookResponse());
//
//        mockMvc.perform(multipart("/books/1/upload").file(file))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testUploadBookImage_FileTooLarge() throws Exception {
//        byte[] largeFileData = new byte[3 * 1024 * 1024]; // 3MB file
//
//        MockMultipartFile largeFile = new MockMultipartFile(
//                "file", "large.jpg", "image/jpeg", largeFileData);
//
//        mockMvc.perform(multipart(UPLOAD_URL).file(largeFile))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error").value("File size must be under 2MB!"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testUploadBookImage_InvalidFileType() throws Exception {
//        MockMultipartFile invalidFile = new MockMultipartFile(
//                "file", "test.txt", "text/plain", "invalid format".getBytes());
//
//        mockMvc.perform(multipart(UPLOAD_URL).file(invalidFile))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error").value("Only PNG and JPG images are allowed!"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testUploadBookImage_CloudinaryUploadFails() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "test.jpg", "image/jpeg", "test image".getBytes());
//
//        when(cloudinaryService.uploadFile(any(), anyString()))
//                .thenThrow(new IOException("Cloudinary error"));
//
//        mockMvc.perform(multipart(UPLOAD_URL).file(file))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.error").value("Error uploading file: Cloudinary error"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testImportDataByCsv() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "books.csv", "text/csv", "id,title,author\n1,Test Book,Test Author".getBytes());
//
//        doNothing().when(bookService).importBooksFromCsv(any());
//
//        mockMvc.perform(multipart("/books/import-csv").file(file))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Add data successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testPreviewBookImage_Found() throws Exception {
//        when(bookService.getBookImageUrl("1")).thenReturn("http://example.com/image.jpg");
//
//        mockMvc.perform(get("/books/1/preview"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testPreviewBookImage_NotFound() throws Exception {
//        when(bookService.getBookImageUrl("1")).thenReturn(null);
//
//        mockMvc.perform(get("/books/1/preview"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.error").value("Image not found"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testDeleteBookImage_Success() throws Exception {
//        doNothing().when(bookService).deleteBookImage("1");
//
//        mockMvc.perform(delete("/books/1/delete-image"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Book image deleted successfully!"));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void testDeleteBookImage_Error() throws Exception {
//        doThrow(new RuntimeException("File not found")).when(bookService).deleteBookImage("1");
//
//        mockMvc.perform(delete("/books/1/delete-image"))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.error").value("Error deleting file: File not found"));
//    }
//}
