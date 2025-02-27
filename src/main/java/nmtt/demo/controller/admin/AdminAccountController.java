package nmtt.demo.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.request.Account.AdminCreationAccountRequest;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.service.account.AccountService;
import nmtt.demo.service.search.account.AccountCriteria;
import nmtt.demo.service.search.account.AccountQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@RestController
@RequestMapping("${admin-mapping}/accounts")
@Slf4j
@RequiredArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AdminAccountController {

    private final AccountService accountService;
    private final AccountQueryService accountQueryService;

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
    public ResponseEntity<ApiResponse<String>> deleteAccountById(@PathVariable("userId") String userId){
        accountService.deleteUserById(userId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .result("Account has been deleted")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Account>> searchUsers(AccountCriteria criteria
            , Pageable pageable) {

        Page<Account> result = accountQueryService.findByCriteria(criteria, pageable);
        return ResponseEntity.ok(result);
    }
}