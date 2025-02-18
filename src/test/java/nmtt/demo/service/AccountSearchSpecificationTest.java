package nmtt.demo.service;

import nmtt.demo.entity.Account;
import nmtt.demo.entity.Borrowing;
import nmtt.demo.entity.Book;
import nmtt.demo.service.account.AccountSearchSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountSearchSpecificationTest {
    @Mock
    private Root<Account> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private Path<String> firstNamePath, lastNamePath, titlePath;
    @Mock
    private Path<LocalDate> dobPath;
    @Mock
    private Join<Account, Borrowing> borrowingsJoin;
    @Mock
    private Join<Borrowing, Book> bookJoin;

    @MockBean
    private AccountSearchSpecification accountSearchSpecification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHasName() {
        when(root.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            if ("firstName".equals(fieldName)) {
                return firstNamePath;
            } else if ("lastName".equals(fieldName)) {
                return lastNamePath;
            }
            return null;
        });

        when(criteriaBuilder.lower(firstNamePath)).thenReturn(firstNamePath);
        when(criteriaBuilder.lower(lastNamePath)).thenReturn(lastNamePath);
        when(criteriaBuilder.like(firstNamePath, "%john%"))
                .thenReturn(mock(Predicate.class));
        when(criteriaBuilder.like(lastNamePath, "%john%"))
                .thenReturn(mock(Predicate.class));
        when(criteriaBuilder.or(any(), any()))
                .thenReturn(mock(Predicate.class));

        Specification<Account> spec = AccountSearchSpecification.hasName("John");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
        assertNotNull(predicate);
    }

    @Test
    void testHasBookTitle() {
        when(root.join(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            if ("borrowings".equals(fieldName)) {
                return borrowingsJoin;
            }
            return null;
        });
        when(borrowingsJoin.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            if ("book".equals(fieldName)) {
                return bookJoin;
            }
            return null;
        });
        when(bookJoin.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            if ("title".equals(fieldName)) {
                return titlePath;
            }
            return null;
        });

        when(criteriaBuilder.lower(titlePath)).thenReturn(titlePath);
        when(criteriaBuilder.like(titlePath, "%harry potter%"))
                .thenReturn(mock(Predicate.class));

        Specification<Account> spec = AccountSearchSpecification.hasBookTitle("Harry Potter");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
        assertNotNull(predicate);
    }

    @Test
    void testIsBornInDateRange() {
        when(root.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            if ("dob".equals(fieldName)) {
                return dobPath;
            }
            return null;
        });

        when(criteriaBuilder.between(dobPath, LocalDate.parse("2000-01-01"), LocalDate.parse("2010-12-31")))
                .thenReturn(mock(Predicate.class));

        Specification<Account> spec = AccountSearchSpecification.isBornInDateRange("2000-01-01", "2010-12-31");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);
        assertNotNull(predicate);
    }
}
