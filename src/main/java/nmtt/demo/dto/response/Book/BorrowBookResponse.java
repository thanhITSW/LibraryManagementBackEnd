package nmtt.demo.dto.response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowBookResponse {
    String id;
    String bookId;

    String title;

    LocalDate borrowDate;

    LocalDate returnDate;

    boolean returned;
}
