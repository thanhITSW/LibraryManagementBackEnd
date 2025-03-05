package nmtt.demo.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.*;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.service.account.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${user-mapping}/accounts")
@Slf4j
@RequiredArgsConstructor
public class UserAccountController {

    private final AccountService accountService;


    @GetMapping("/my-info")
    public ResponseEntity<AccountResponse> getMyInfo() {
        AccountResponse accountResponse = accountService.getMyInfo();
        return ResponseEntity.ok(accountResponse);
    }

    @PostMapping("/request-change-mail")
    public ResponseEntity<ApiResponse<String>> requestChangeMail(@RequestBody EmailRequest request) {
        accountService.requestChangeMail(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Verification code has been sent to the new email")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-change-mail")
    public ResponseEntity<ApiResponse<String>> verifyChangeMail(@RequestBody VerifyCodeRequest request) {
        accountService.verifyChangeMail(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Email has been successfully updated")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Password has been successfully updated")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-change-phone")
    public ResponseEntity<ApiResponse<String>> requestChangePhone(
            @RequestBody ChangePhoneRequest request){

        String otp = accountService.requestChangePhone(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("OTP sent successfully with otp: " + otp)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-change-phone")
    public ResponseEntity<ApiResponse<String>> verifyChangePhone(
            @RequestBody VerifyCodeRequest request){

        accountService.verifyChangePhone(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Phone number has been successfully updated")
                .build();

        return ResponseEntity.ok(response);
    }
}