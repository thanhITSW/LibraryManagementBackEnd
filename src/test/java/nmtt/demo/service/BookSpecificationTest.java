package nmtt.demo.service;
import jakarta.persistence.criteria.*;
import nmtt.demo.entity.Book;
import nmtt.demo.service.book.BookSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class BookSpecificationTest {
    @Mock
    private Root<Book> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Path<String> titlePath, authorPath, categoryPath;

    @MockBean
    private BookSpecification bookSpecification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchByKeyword() {
        // Mocking root.get() to return the appropriate paths
        when(root.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            return switch (fieldName) {
                case "title" -> titlePath;
                case "author" -> authorPath;
                case "category" -> categoryPath;
                default -> null;
            };
        });

        // Mocking lower() calls
        when(criteriaBuilder.lower(titlePath)).thenReturn(titlePath);
        when(criteriaBuilder.lower(authorPath)).thenReturn(authorPath);
        when(criteriaBuilder.lower(categoryPath)).thenReturn(categoryPath);

        // Mocking like() predicates
        when(criteriaBuilder.like(titlePath, "%harry potter%")).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.like(authorPath, "%harry potter%")).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.like(categoryPath, "%harry potter%")).thenReturn(mock(Predicate.class));

        // Mocking OR condition
        when(criteriaBuilder.or(any(), any(), any())).thenReturn(mock(Predicate.class));

        // Execute Specification
        Specification<Book> spec = BookSpecification.searchByKeyword("Harry Potter");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        // Verify result
        assertNotNull(predicate);
    }
}
