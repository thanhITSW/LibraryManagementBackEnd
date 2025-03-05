package nmtt.demo.service.borrowing;

import nmtt.demo.dto.request.Book.BookRequest;
import nmtt.demo.dto.response.Book.BorrowBookResponse;
import nmtt.demo.entity.Book;
import java.util.List;

public interface BorrowingService {
    public void borrowBook(BookRequest request);
    public void returnBook(BookRequest request);
    public List<BorrowBookResponse> getBorrowedBooks();
}
