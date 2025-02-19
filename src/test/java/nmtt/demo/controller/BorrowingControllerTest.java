package nmtt.demo.controller;

import nmtt.demo.entity.Account;
import nmtt.demo.entity.Book;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.service.borrowing.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowingService borrowingService;

    private Account mockAccount;
    private Book mockBook;
    private Borrowing mockBorrowing;

    @BeforeEach
    void setup() {
        //create data account
        mockAccount = Account.builder()
                .id(UUID.randomUUID().toString())
                .email("user@example.com")
                .password("securepassword")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(2000, 1, 1))
                .active(true)
                .build();

        // create data book
        mockBook = Book.builder()
                .id(UUID.randomUUID().toString())
                .title("Java Programming")
                .author("John Doe")
                .category("Technology")
                .totalCopies(10)
                .availableCopies(5)
                .imageUrl("https://example.com/java.jpg")
                .available(true)
                .build();

        // create data Borrowing
        mockBorrowing = Borrowing.builder()
                .id(UUID.randomUUID().toString())
                .account(mockAccount)
                .book(mockBook)
                .borrowDate(LocalDate.now())
                .returned(false)
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testBorrowBook_Success() throws Exception {
        doNothing().when(borrowingService).borrowBook(mockAccount.getId(), mockBook.getId());

        mockMvc.perform(post("/borrowing/borrow")
                        .param("accountId", mockAccount.getId())
                        .param("bookId", mockBook.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Book borrowed successfully!"));

        verify(borrowingService, times(1)).borrowBook(mockAccount.getId(), mockBook.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testBorrowBook_IllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Book is not available"))
                .when(borrowingService).borrowBook(mockAccount.getId(), mockBook.getId());

        mockMvc.perform(post("/borrowing/borrow")
                        .param("accountId", mockAccount.getId())
                        .param("bookId", mockBook.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Book is not available"));

        verify(borrowingService, times(1)).borrowBook(mockAccount.getId(), mockBook.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testReturnBook_Success() throws Exception {
        doNothing().when(borrowingService).returnBook(mockAccount.getId(), mockBook.getId());

        mockMvc.perform(post("/borrowing/return")
                        .param("accountId", mockAccount.getId())
                        .param("bookId", mockBook.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Book returned successfully!"));

        verify(borrowingService, times(1)).returnBook(mockAccount.getId(), mockBook.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testReturnBook_IllegalArgumentException() throws Exception {
        doThrow(new IllegalArgumentException("Book is not borrowed by this account"))
                .when(borrowingService).returnBook(mockAccount.getId(), mockBook.getId());

        mockMvc.perform(post("/borrowing/return")
                        .param("accountId", mockAccount.getId())
                        .param("bookId", mockBook.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Book is not borrowed by this account"));

        verify(borrowingService, times(1)).returnBook(mockAccount.getId(), mockBook.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetBorrowedBooks_HasBooks() throws Exception {
        when(borrowingService.getBorrowedBooks(mockAccount.getId()))
                .thenReturn(List.of(mockBook));

        mockMvc.perform(get("/borrowing/" + mockAccount.getId() + "/borrowed-books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(mockBook.getId()))
                .andExpect(jsonPath("$[0].title").value(mockBook.getTitle()))
                .andExpect(jsonPath("$[0].available").value(true));

        verify(borrowingService, times(1)).getBorrowedBooks(mockAccount.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetBorrowedBooks_NoBooks() throws Exception {
        when(borrowingService.getBorrowedBooks(mockAccount.getId()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/borrowing/" + mockAccount.getId() + "/borrowed-books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("No borrowed books found."));

        verify(borrowingService, times(1)).getBorrowedBooks(mockAccount.getId());
    }
}