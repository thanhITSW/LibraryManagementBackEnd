package nmtt.demo.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.request.Account.AdminCreationAccountRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.service.account.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${admin-mapping}/accounts")
@Slf4j
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllUsers() {
        List<AccountResponse> accounts = accountService.getAccount();

        return ResponseEntity.ok(accounts);
    }


    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AdminCreationAccountRequest request) {
        AccountResponse accountResponse = accountService.adminCreateAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }


    @PutMapping("/{userId}")
    public ResponseEntity<AccountResponse> updateAccountById(
            @PathVariable("userId") String userId,
            @RequestBody @Valid AccountUpdateRequest request) {

        AccountResponse updatedAccount = accountService.updateAccountById(userId, request);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteAccountById(@PathVariable("userId") String userId){
        accountService.deleteUserById(userId);

        return ResponseEntity.ok("Account has been deleted");
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AccountResponse> responses = accountService.searchMember(name, bookTitle, dateFrom, dateTo, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("data", responses.getContent());
        response.put("currentPage", responses.getNumber());
        response.put("totalItems", responses.getTotalElements());
        response.put("totalPages", responses.getTotalPages());

        return ResponseEntity.ok(response);
    }
}