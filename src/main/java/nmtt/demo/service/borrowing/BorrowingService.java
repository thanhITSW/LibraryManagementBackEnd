package nmtt.demo.service.borrowing;

import nmtt.demo.entity.Book;
import java.util.List;

public interface BorrowingService {
    public void borrowBook(String accountId, String bookId);
    public void returnBook(String accountId, String bookId);
    public List<Book> getBorrowedBooks(String accountId);
}
