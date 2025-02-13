package nmtt.demo.service.account;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountCreationRequest request);
    List<AccountResponse> getAccount();
    AccountResponse getMyInfo();
    AccountResponse updateAccountById(String accountId, AccountUpdateRequest request);
    void deleteUserById(String accountId);
    void resetPass(String email);
    void requestChangeMail(String accountId, String newEmail);
    void verifyChangeMail(String accountId, String verificationCode);
    Page<AccountResponse> searchMember(String name, String bookTitle, String dateFrom, String dateTo, int page, int size);

}
