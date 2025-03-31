package nmtt.demo.service.search.account;

import lombok.Data;
import tech.jhipster.service.filter.*;

import java.io.Serializable;

@Data
public class AccountCriteria implements Serializable {
    private StringFilter email;
    // Supports: equals, contains, doesNotContain, in, notIn, specified
    private StringFilter firstName;

    private StringFilter lastName;

    // Supports: equals, greaterThan, greaterThanOrEqual, lessThan, lessThanOrEqual, between, specified
    private LocalDateFilter dob;

    private StringFilter phone;

    // Supports: equals, specified
    private BooleanFilter active;

    private StringFilter bookTitle;
}
