package nmtt.demo.dto.request.Borrowing;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowingReportRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
}
