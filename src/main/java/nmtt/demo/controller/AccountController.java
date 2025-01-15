package nmtt.demo.controller;

import jakarta.validation.Valid;
import nmtt.demo.dto.request.AccountCreationRequest;
import nmtt.demo.dto.request.AccountUpdateRequest;
import nmtt.demo.dto.request.ApiResponse;
import nmtt.demo.dto.response.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAllUsers() {
        return accountService.getAccount();
    }

    @PostMapping
    public ApiResponse<Account> createAccount(@RequestBody @Valid AccountCreationRequest request){
        ApiResponse<Account> apiResponse = new ApiResponse<>();

        apiResponse.setResult(accountService.createRequest(request));
        return apiResponse;
    }

    @GetMapping("/{userId}")
    public Account getAccountById(@PathVariable("userId") String userId){
        return accountService.getAccountById(userId);
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
