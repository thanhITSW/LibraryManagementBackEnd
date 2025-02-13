package nmtt.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "borrowings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    String id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Column(name = "borrow_date", nullable = false)
    LocalDate borrowDate;

    @Column(name = "return_date")
    LocalDate returnDate;

    @Column(name = "returned", nullable = false)
    boolean returned = false;
}
