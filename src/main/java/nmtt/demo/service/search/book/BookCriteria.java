package nmtt.demo.service.search.book;

import lombok.Data;
import tech.jhipster.service.filter.*;

import java.io.Serializable;

@Data
public class BookCriteria implements Serializable{
    // Supports: equals, contains, doesNotContain, in, notIn, specified
    private StringFilter title;

    private StringFilter author;

    private StringFilter category;

    // Supports: equals, specified
    private BooleanFilter available;
}
