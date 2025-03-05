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


    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AccountCreationRequest request) {
        AccountResponse accountResponse = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @PostMapping("/reset-pass")
    public ResponseEntity<ApiResponse<String>> resetPass(@RequestBody EmailRequest request){
        accountService.resetPass(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("New password has been send your email")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-link-active-account")
    public ResponseEntity<ApiResponse<String>> resendLinkActiveAccount(@RequestBody EmailRequest request){
        accountService.resendLinkActiveAccount(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Link active account has been send your email")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-info")
    public ResponseEntity<AccountResponse> getMyInfo() {
        AccountResponse accountResponse = accountService.getMyInfo();
        return ResponseEntity.ok(accountResponse);
    }
}