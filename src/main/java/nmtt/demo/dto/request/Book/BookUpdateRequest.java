package nmtt.demo.dto.request.Book;

import lombok.Data;

@Data
public class BookUpdateRequest {
    String title;
    String author;
    String category;
    int totalCopies;
    int availableCopies;
    boolean available;
}
