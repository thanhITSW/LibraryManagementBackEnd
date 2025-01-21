package nmtt.demo.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.service.AccountService;
import nmtt.demo.service.EmailSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {

    AccountService accountService;

    @GetMapping
    ApiResponse<List<AccountResponse>> getAllUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        authentication.getAuthorities()
                .forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<AccountResponse>>builder()
                .result(accountService.getAccount())
                .build();
    }

    @PostMapping
    public ApiResponse<AccountResponse> createAccount(@RequestBody @Valid AccountCreationRequest request){
        ApiResponse<AccountResponse> apiResponse = new ApiResponse<>();

        apiResponse.setResult(accountService.createAccount(request));
        return apiResponse;
    }

    @GetMapping("/myInfo")
    ApiResponse<AccountResponse> getMyInfo(){
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.getMyInfo())
                .build();
    }

    @PutMapping("/{userId}")
    public AccountResponse updateAccountById(@PathVariable("userId") String userId, @RequestBody AccountUpdateRequest request){
        return accountService.updateAccountById(userId, request);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<String> deleteAccountById(@PathVariable("userId") String userId){
        accountService.deleteUserById(userId);
        return ApiResponse.<String>builder().result("Account has been deleted").build();
    }

    @PostMapping("/resetPass")
    public ApiResponse<String> resetPass(@RequestParam String email){
        accountService.resetPass(email);
        return ApiResponse.<String>builder().result("New password has been send your email").build();
    }

    @PostMapping("/requestChangeMail")
    public ApiResponse<String> requestChangeMail(@RequestParam("accountId") String accountId, @RequestParam("newEmail") String newEmail) {
        accountService.requestChangeMail(accountId, newEmail);
        return ApiResponse.<String>builder().result("Verification code has been sent to the new email").build();
    }

    @PostMapping("/verifyChangeMail")
    public ApiResponse<String> verifyChangeMail(@RequestParam("accountId") String accountId, @RequestParam("verificationCode") String verificationCode) {
        accountService.verifyChangeMail(accountId, verificationCode);
        return ApiResponse.<String>builder().result("Email has been successfully updated").build();
    }
}
