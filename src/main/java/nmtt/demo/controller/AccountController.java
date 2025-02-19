package nmtt.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.service.account.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        authentication.getAuthorities()
                .forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        List<AccountResponse> accounts = accountService.getAccount();

        return ResponseEntity.ok(accounts);
    }


    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AccountCreationRequest request) {
        AccountResponse accountResponse = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }


    @GetMapping("/my-info")
    public ResponseEntity<AccountResponse> getMyInfo() {
        AccountResponse accountResponse = accountService.getMyInfo();
        return ResponseEntity.ok(accountResponse);
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

    @PostMapping("/reset-pass")
    public ResponseEntity<String> resetPass(@RequestParam String email){
        accountService.resetPass(email);

        return ResponseEntity.ok("New password has been send your email");
    }

    @PostMapping("/request-change-mail")
    public ResponseEntity<String> requestChangeMail(@RequestParam("accountId") String accountId, @RequestParam("newEmail") String newEmail) {
        accountService.requestChangeMail(accountId, newEmail);

        return ResponseEntity.ok("Verification code has been sent to the new email");
    }

    @PostMapping("/verify-change-mail")
    public ResponseEntity<String> verifyChangeMail(@RequestParam("accountId") String accountId, @RequestParam("verificationCode") String verificationCode) {
        accountService.verifyChangeMail(accountId, verificationCode);

        return ResponseEntity.ok("Email has been successfully updated");
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