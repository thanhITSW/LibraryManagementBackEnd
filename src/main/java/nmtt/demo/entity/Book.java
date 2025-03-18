package nmtt.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    String id;

    @Column(name = "title", unique = true)
    String title;

    @Column(name = "author")
    String author;

    @Column(name = "category")
    String category;

    @Column(name = "total_copies")
    int totalCopies;

    @Column(name = "available_copies")
    int availableCopies;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "image_public_id")
    String imagePublicId;

    @Builder.Default
    @Column(name = "available")
    boolean available = true;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Borrowing> borrowings;
}