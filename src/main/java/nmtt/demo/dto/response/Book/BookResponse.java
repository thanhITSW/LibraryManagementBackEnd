package nmtt.demo.dto.response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    String id;
    String title;
    String author;
    String category;
    int totalCopies;
    int availableCopies;
    boolean available;
}
