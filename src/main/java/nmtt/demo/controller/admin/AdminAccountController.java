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

    /**
     * Retrieves a list of all user accounts.
     *
     * @return A ResponseEntity containing a list of AccountResponse objects.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllUsers() {
        List<AccountResponse> accounts = accountService.getAccount();

        return ResponseEntity.ok(accounts);
    }

    /**
     * Creates a new user account using the provided request data.
     *
     * @param request The request object containing the necessary data for creating a new user account.
     *                This object should be validated using the {@link Valid} annotation.
     *
     * @return A ResponseEntity containing the newly created AccountResponse object.
     *         The HTTP status code is 201 (Created) if the operation is successful.
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AdminCreationAccountRequest request) {
        AccountResponse accountResponse = accountService.adminCreateAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    /**
     * Retrieves a user account by its unique identifier.
     *
     * @param userId The unique identifier of the user account to retrieve.
     *               This parameter is obtained from the URL path variable.
     *
     * @return A ResponseEntity containing the requested AccountResponse object.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     *         If the user account with the specified identifier does not exist,
     *         the HTTP status code is 404 (Not Found).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable("userId") String userId) {
        AccountResponse accountResponse = accountService.getAccountById(userId);
        return ResponseEntity.ok(accountResponse);
    }

    /**
     * Updates an existing user account using the provided request data.
     *
     * @param userId The unique identifier of the user account to update.
     *               This parameter is obtained from the URL path variable.
     * @param request The request object containing the necessary data for updating the user account.
     *                This object should be validated using the {@link Valid} annotation.
     *
     * @return A ResponseEntity containing the updated AccountResponse object.
     *         The HTTP status code is 200 (OK) if the operation is successful.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<AccountResponse> updateAccountById(
            @PathVariable("userId") String userId,
            @RequestBody @Valid AccountUpdateRequest request) {

        AccountResponse updatedAccount = accountService.updateAccountById(userId, request);
        return ResponseEntity.ok(updatedAccount);
    }

    /**
     * Deletes a user account by its ID.
     *
     * @param userId The ID of the user to delete.
     * @return Response indicating the account has been deleted.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteAccountById(@PathVariable("userId") String userId){
        accountService.deleteUserById(userId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Account has been deleted")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Searches for user accounts based on given criteria with pagination support.
     *
     * @param criteria The criteria to filter accounts.
     * @param pageable Pagination information.
     * @return A page of accounts matching the criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Account>> searchUsers(AccountCriteria criteria
            , Pageable pageable) {

        Page<Account> result = accountQueryService.findByCriteria(criteria, pageable);
        return ResponseEntity.ok(result);
    }
}