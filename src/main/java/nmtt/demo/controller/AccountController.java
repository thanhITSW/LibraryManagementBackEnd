package nmtt.demo.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.AccountCreationRequest;
import nmtt.demo.dto.request.AccountUpdateRequest;
import nmtt.demo.dto.request.ApiResponse;
import nmtt.demo.dto.response.AccountResponse;
import nmtt.demo.service.AccountService;
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

        log.info("Username: {}", authentication.getName());
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
    public String deleteAccountById(@PathVariable("userId") String userId){
        accountService.deleteUserById(userId);
        return "Account has been deleted";
    }
}
