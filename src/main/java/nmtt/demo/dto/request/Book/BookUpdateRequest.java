package nmtt.demo.dto.request.Book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookUpdateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 1, message = "Total copies must be at least 1")
    private int totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    private int availableCopies;

    private boolean available;
}