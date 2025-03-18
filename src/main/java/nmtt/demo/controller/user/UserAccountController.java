package nmtt.demo.controller.user;

import jakarta.validation.Valid;
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

    /**
     * Retrieves the user's account information.
     *
     * @return A ResponseEntity containing the user's account information wrapped in an {@link AccountResponse} object.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     */
    @GetMapping("/my-info")
    public ResponseEntity<AccountResponse> getMyInfo() {
        AccountResponse accountResponse = accountService.getMyInfo();
        return ResponseEntity.ok(accountResponse);
    }

    /**
     * Sends a verification code to the new email address for account change.
     *
     * @param request The request object containing the new email address.
     *                It is assumed that the email address is validated and not null.
     *
     * @return A ResponseEntity containing an {@link ApiResponse} object.
     *         The ApiResponse contains a message indicating that the verification code has been sent to the new email.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     */
    @PostMapping("/request-change-mail")
    public ResponseEntity<ApiResponse<String>> requestChangeMail(@RequestBody @Valid EmailRequest request) {
        accountService.requestChangeMail(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Verification code has been sent to the new email")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the new email address for account change by using the verification code sent to the new email.
     *
     * @param request The request object containing the verification code.
     *                It is assumed that the verification code is validated and not null.
     *
     * @return A ResponseEntity containing an {@link ApiResponse} object.
     *         The ApiResponse contains a message indicating that the email has been successfully updated.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     */
    @PostMapping("/verify-change-mail")
    public ResponseEntity<ApiResponse<String>> verifyChangeMail(@RequestBody VerifyCodeRequest request) {
        accountService.verifyChangeMail(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Email has been successfully updated")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the user's password.
     *
     * @param request The request object containing the new password and its confirmation.
     *                It is assumed that the password and its confirmation are validated and not null.
     *
     * @return A ResponseEntity containing an {@link ApiResponse} object.
     *         The ApiResponse contains a message indicating that the password has been successfully updated.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Password has been successfully updated")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Sends an OTP (One Time Password) to the user's new phone number for account change.
     *
     * @param request The request object containing the new phone number.
     *                It is assumed that the phone number is validated and not null.
     *
     * @return A ResponseEntity containing an {@link ApiResponse} object.
     *         The ApiResponse contains a message indicating that the OTP has been sent to the new phone number.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     *         The OTP is also returned in the response for further verification.
     */
    @PostMapping("/request-change-phone")
    public ResponseEntity<ApiResponse<String>> requestChangePhone(
            @RequestBody @Valid ChangePhoneRequest request){

        String otp = accountService.requestChangePhone(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("OTP sent successfully with otp: " + otp)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the new phone number for account change by using the OTP (One Time Password) sent to the new phone number.
     *
     * @param request The request object containing the OTP.
     *                It is assumed that the OTP is validated and not null.
     *
     * @return A ResponseEntity containing an {@link ApiResponse} object.
     *         The ApiResponse contains a message indicating that the phone number has been successfully updated.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     */
    @PostMapping("/verify-change-phone")
    public ResponseEntity<ApiResponse<String>> verifyChangePhone(
            @RequestBody VerifyCodeRequest request){

        accountService.verifyChangePhone(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Phone number has been successfully updated")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Changes the user's password during their first login after account creation.
     *
     * @param request The request object containing the new password and its confirmation.
     *                It is assumed that the password and its confirmation are validated and not null.
     *
     * @return A ResponseEntity containing an {@link ApiResponse} object.
     *         The ApiResponse contains a message indicating that the password has been successfully updated.
     *         The HTTP status code will be 200 (OK) if the request is successful.
     */
    @PostMapping("/change-password-first-login")
    public ResponseEntity<ApiResponse<String>> changePasswordFirstLogin (
            @RequestBody @Valid FirstLoginRequest request){
        accountService.changePasswordFirstLogin(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Change password successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}