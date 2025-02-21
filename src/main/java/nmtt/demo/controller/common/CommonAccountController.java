package nmtt.demo.controller.common;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
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
    public ResponseEntity<String> resetPass(@RequestBody EmailRequest request){
        accountService.resetPass(request);

        return ResponseEntity.ok("New password has been send your email");
    }
}