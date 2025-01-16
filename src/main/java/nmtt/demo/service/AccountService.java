package nmtt.demo.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import nmtt.demo.dto.request.AccountCreationRequest;
import nmtt.demo.dto.request.AccountUpdateRequest;
import nmtt.demo.dto.response.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.enums.Role;
import nmtt.demo.exception.AppException;
import nmtt.demo.exception.ErrorCode;
import nmtt.demo.mapper.AccountMapper;
import nmtt.demo.repository.AccountRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;

    public AccountResponse createAccount(AccountCreationRequest request){
//        Account account = new Account();

        if(accountRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        account.setRoles(roles);

        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountResponse> getAccount(){
        return accountRepository.findAll().stream().map(accountMapper::toAccountResponse).toList();
    }

    @PostAuthorize("returnObject.email == authentication.name")
    public AccountResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return accountMapper.toAccountResponse(account);
    }

    @PostAuthorize("returnObject.email == authentication.name")
    public Account getAccountById(String id){
        return accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found") );
    }

    public AccountResponse updateAccountById(String accountId, AccountUpdateRequest request){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found") );;

        accountMapper.updateAccount(account, request);
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    public void deleteUserById(String accountId){
        accountRepository.deleteById(accountId);
    }
}
