package nmtt.demo.service.account;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nmtt.demo.constant.PredefinedRole;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.EmailVerification;
import nmtt.demo.entity.Role;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.mapper.AccountMapper;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.EmailVerificationRepository;
import nmtt.demo.repository.RoleRepository;
import nmtt.demo.service.email.EmailSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailSenderService emailSenderService;
    private final EmailVerificationRepository emailVerificationRepository;

    @Value("${URL_API}")
    private String urlApi;

    /**
     * Creates a new user account.
     *
     * @param request The account creation request containing user details.
     * @return The created account as an AccountResponse.
     * @throws AppException If the email is already registered.
     */
    @Transactional
    @Override
    public AccountResponse createAccount(AccountCreationRequest request){

        if(accountRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Account account = accountMapper.toAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        account.setRoles(roles);
        account = accountRepository.save(account);

        emailSenderService.sendSimpleEmail(account.getEmail(), "Verify account"
                , "Please click here to verify account: " + urlApi + "auth/" + account.getId());

        return accountMapper.toAccountResponse(account);
    }

    /**
     * Retrieves a list of all user accounts.
     *
     * @return A list of AccountResponse objects representing all accounts.
     */
    @Override
    public List<AccountResponse> getAccount(){
        return accountRepository
                .findAll()
                .stream()
                .map(accountMapper::toAccountResponse).toList();
    }

    /**
     * Retrieves the authenticated user's account information.
     *
     * @return The authenticated user's account details as an AccountResponse.
     * @throws AppException If the user does not exist.
     */
    @Override
    public AccountResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        Account account = accountRepository
                .findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return accountMapper.toAccountResponse(account);
    }

    /**
     * Updates an existing account by its ID.
     *
     * @param accountId The ID of the account to be updated.
     * @param request The request containing updated account details.
     * @return The updated account as an AccountResponse.
     * @throws RuntimeException If the account is not found.
     */
    @Transactional
    @Override
    public AccountResponse updateAccountById(String accountId, AccountUpdateRequest request){
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found") );;

        accountMapper.updateAccount(account, request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository
                .findAllById(request.getRoles());

        account.setRoles(new HashSet<>(roles));
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    /**
     * Deletes a user account by its ID.
     *
     * @param accountId The ID of the account to be deleted.
     */
    @Transactional
    @Override
    public void deleteUserById(String accountId){
        accountRepository.deleteById(accountId);
    }

    /**
     * Resets the user's password and sends the new password via email.
     *
     * @param email The email address of the user requesting a password reset.
     * @throws AppException If the user does not exist.
     */
    @Override
    public void resetPass(String email) {
        Account account = accountRepository
                .findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newPassword = String.format("%06d", (int) (Math.random() * 1000000));
        account.setPassword(passwordEncoder.encode(newPassword));

        accountRepository.save(account);

        String subject = "Reset Password";
        String message = "Your password has been reset. Your new password is: " + newPassword;
        emailSenderService.sendSimpleEmail(email, subject, message);
    }

    /**
     * Requests an email change by generating a verification code and sending it to the new email.
     *
     * @param accountId The ID of the account requesting the email change.
     * @param newEmail The new email address to be verified.
     * @throws AppException If the user does not exist.
     */
    @Override
    public void requestChangeMail(String accountId, String newEmail) {
        accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

        EmailVerification verification = new EmailVerification();
        verification.setAccountId(accountId);
        verification.setNewEmail(newEmail);
        verification.setVerificationCode(verificationCode);

        emailVerificationRepository.save(verification);

        // send email authentication
        String subject = "Verify New Email Address";
        String message = "Please use the following code to verify your new email address: " + verificationCode;
        emailSenderService.sendSimpleEmail(newEmail, subject, message);
    }

    /**
     * Verifies the email change request using the provided verification code and updates the user's email address.
     *
     * @param accountId The ID of the account for which the email change is being verified.
     * @param verificationCode The verification code sent to the new email.
     * @throws AppException If the verification code is invalid or the user does not exist.
     */
    @Override
    public void verifyChangeMail(String accountId, String verificationCode) {
        EmailVerification verification = emailVerificationRepository
                .findByAccountIdAndVerificationCode(accountId, verificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_CODE));

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setEmail(verification.getNewEmail());
        accountRepository.save(account);

        emailVerificationRepository.delete(verification);

        String subject = "Email Changed Successfully";
        String message = "Your email address has been successfully updated.";
        emailSenderService.sendSimpleEmail(account.getEmail(), subject, message);
    }

    /**
     * Searches for members based on the provided criteria: name, book title, and birth date range.
     *
     * @param name The name of the member to search for.
     * @param bookTitle The book title associated with the member.
     * @param dateFrom The start date of the birth date range.
     * @param dateTo The end date of the birth date range.
     * @param page The page number for pagination.
     * @param size The size of each page.
     * @return A page of AccountResponse objects that match the search criteria.
     */
    @Override
    public Page<AccountResponse> searchMember(
            String name,
            String bookTitle,
            String dateFrom,
            String dateTo,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Account> spec = Specification
                .where(AccountSearchSpecification.hasName(name))
                .and(AccountSearchSpecification.hasBookTitle(bookTitle))
                .and(AccountSearchSpecification.isBornInDateRange(dateFrom, dateTo));

        Page<Account> accounts = accountRepository.findAll(spec, pageable);

        return accounts.map(accountMapper::toAccountResponse);
    }
}
