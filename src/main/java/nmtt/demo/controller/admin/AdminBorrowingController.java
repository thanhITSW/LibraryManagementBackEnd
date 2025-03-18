package nmtt.demo.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Borrowing.BorrowingReportRequest;
import nmtt.demo.service.borrowing.BorrowingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${admin-mapping}/borrowing")
@Slf4j
@RequiredArgsConstructor
public class AdminBorrowingController {
    private final BorrowingService borrowingService;

    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> getBorrowingReport(@RequestBody BorrowingReportRequest request) {
        Map<String, Object> report = borrowingService.getBorrowingReport(request.getFromDate(), request.getToDate());
        return ResponseEntity.ok(report);
    }
}
