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
    public ResponseEntity<String> requestChangeMail(@RequestBody EmailRequest request) {
        accountService.requestChangeMail(request);

        return ResponseEntity.ok("Verification code has been sent to the new email");
    }

    @PostMapping("/verify-change-mail")
    public ResponseEntity<String> verifyChangeMail(@RequestBody VerifyCodeRequest request) {
        accountService.verifyChangeMail(request);

        return ResponseEntity.ok("Email has been successfully updated");
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);

        return ResponseEntity.ok("Password has been successfully updated");
    }

    @PostMapping("/request-change-phone")
    public ResponseEntity<String> requestChangePhone(
            @RequestBody ChangePhoneRequest request){

        String otp = accountService.requestChangePhone(request);

        return ResponseEntity.ok("OTP sent successfully with otp: " + otp);
    }

    @PostMapping("/verify-change-phone")
    public ResponseEntity<String> verifyChangePhone(
            @RequestBody VerifyCodeRequest request){

        accountService.verifyChangePhone(request);

        return ResponseEntity.ok("Phone number has been successfully updated");
    }
}