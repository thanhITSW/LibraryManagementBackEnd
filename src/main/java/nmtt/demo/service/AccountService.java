package nmtt.demo.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import nmtt.demo.dto.request.AccountCreationRequest;
import nmtt.demo.dto.request.AccountUpdateRequest;
import nmtt.demo.dto.response.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.exception.AppException;
import nmtt.demo.exception.ErrorCode;
import nmtt.demo.mapper.AccountMapper;
import nmtt.demo.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    AccountRepository accountRepository;

    AccountMapper accountMapper;

    public Account createRequest(AccountCreationRequest request){
//        Account account = new Account();

        if(accountRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Account account = accountMapper.toAccount(request);

//        account.setUsername(request.getUsername());
//        account.setLastName(request.getLastName());
//        account.setFirstName(request.getFirstName());
//        account.setPassword(request.getPassword());
//        account.setDob(request.getDob());

        return accountRepository.save(account);
    }

    public List<Account> getAccount(){
        return accountRepository.findAll();
    }

    public Account getAccountById(String id){
        return accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found") );
    }

    public AccountResponse updateAccountById(String accountId, AccountUpdateRequest request){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found") );;

//        account.setDob(request.getDob());
//        account.setPassword(request.getPassword());
//        account.setFirstName(request.getFirstName());
//        account.setLastName(request.getLastName());

        accountMapper.updateAccount(account, request);
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    public void deleteUserById(String accountId){
        accountRepository.deleteById(accountId);
    }
}
