package nmtt.demo.controller.common;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.request.Account.EmailRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.service.account.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${common-mapping}/accounts")
@Slf4j
@RequiredArgsConstructor
public class CommonAccountController {

    private final AccountService accountService;


    /**
     * Creates a new account using the provided account creation request.
     *
     * @param request The account creation request containing the necessary information to create a new account.
     * @return A ResponseEntity containing the created account's information with a HTTP status code of 201 (Created).
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AccountCreationRequest request) {
        AccountResponse accountResponse = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    /**
     * Resets the password of the account associated with the provided email.
     *
     * @param request The email request containing the email of the account to reset the password.
     * @return A ResponseEntity containing a success message with a HTTP status code of 200 (OK).
     *         The success message indicates that a new password has been sent to the provided email.
     */
    @PostMapping("/reset-pass")
    public ResponseEntity<ApiResponse<String>> resetPass(@RequestBody EmailRequest request){
        accountService.resetPass(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("New password has been send your email")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Resends the activation link to the account associated with the provided email.
     *
     * @param request The email request containing the email of the account to resend the activation link.
     * @return A ResponseEntity containing a success message with a HTTP status code of 200 (OK).
     *         The success message indicates that the activation link has been sent to the provided email.
     */
    @PostMapping("/resend-link-active-account")
    public ResponseEntity<ApiResponse<String>> resendLinkActiveAccount(@RequestBody EmailRequest request){
        accountService.resendLinkActiveAccount(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Link active account has been send your email")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the information of the currently authenticated account.
     *
     * @return A ResponseEntity containing the account information with a HTTP status code of 200 (OK).
     *         The account information is encapsulated in an AccountResponse object.
     */
    @GetMapping("/my-info")
    public ResponseEntity<AccountResponse> getMyInfo() {
        AccountResponse accountResponse = accountService.getMyInfo();
        return ResponseEntity.ok(accountResponse);
    }
}