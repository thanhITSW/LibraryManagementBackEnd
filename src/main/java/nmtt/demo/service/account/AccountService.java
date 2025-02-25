package nmtt.demo.service.account;
import nmtt.demo.dto.request.Account.*;
import nmtt.demo.dto.response.Account.AccountResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountCreationRequest request);
    public AccountResponse adminCreateAccount(AdminCreationAccountRequest request);
    List<AccountResponse> getAccount();
    AccountResponse getMyInfo();
    AccountResponse updateAccountById(String accountId, AccountUpdateRequest request);
    void deleteUserById(String accountId);
    void resetPass(EmailRequest request);
    void requestChangeMail(EmailRequest request);
    void verifyChangeMail(VerifyCodeRequest request);
    Page<AccountResponse> searchMember(String name, String bookTitle, String dateFrom, String dateTo, int page, int size);
    void changePassword(ChangePasswordRequest request);

    String requestChangePhone(ChangePhoneRequest request);
    void verifyChangePhone(VerifyCodeRequest request);

    void resendLinkActiveAccount(EmailRequest request);
}
