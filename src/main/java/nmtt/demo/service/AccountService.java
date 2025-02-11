package nmtt.demo.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import nmtt.demo.constant.PredefinedRole;
import nmtt.demo.dto.request.Account.AccountCreationRequest;
import nmtt.demo.dto.request.Account.AccountUpdateRequest;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.EmailVerification;
import nmtt.demo.entity.Role;
import nmtt.demo.exception.AppException;
import nmtt.demo.exception.ErrorCode;
import nmtt.demo.mapper.AccountMapper;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.EmailVerificationRepository;
import nmtt.demo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountService {
    final AccountRepository accountRepository;
    final AccountMapper accountMapper;
    final PasswordEncoder passwordEncoder;
    final RoleRepository roleRepository;
    final EmailSenderService emailSenderService;
    final EmailVerificationRepository emailVerificationRepository;

    @Value("${URL_API}")
    String urlApi;

    @Transactional
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

    @PreAuthorize("hasRole('ADMIN')")
//    @PreAuthorize("hasAuthority('APPROVE_POST')")   //phân quyền chức năng
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

    @Transactional
    public AccountResponse updateAccountById(String accountId, AccountUpdateRequest request){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found") );;

        accountMapper.updateAccount(account, request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        account.setRoles(new HashSet<>(roles));
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    @Transactional
    public void deleteUserById(String accountId){
        accountRepository.deleteById(accountId);
    }

    public void resetPass(String email){
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newPassword = String.format("%06d", (int) (Math.random() * 1000000));

        account.setPassword(passwordEncoder.encode(newPassword));

        accountRepository.save(account);

        String subject = "Reset Password";
        String message = "Your password has been reset. Your new password is: " + newPassword;
        emailSenderService.sendSimpleEmail(email, subject, message);
    }

    @PreAuthorize("hasRole('USER')")
    public void requestChangeMail(String accountId, String newEmail) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

        EmailVerification verification = new EmailVerification();
        verification.setAccountId(accountId);
        verification.setNewEmail(newEmail);
        verification.setVerificationCode(verificationCode);

        emailVerificationRepository.save(verification);

        // Gửi email xác thực
        String subject = "Verify New Email Address";
        String message = "Please use the following code to verify your new email address: " + verificationCode;
        emailSenderService.sendSimpleEmail(newEmail, subject, message);
    }

    @PreAuthorize("hasRole('USER')")
    public void verifyChangeMail(String accountId, String verificationCode) {
        EmailVerification verification = emailVerificationRepository.findByAccountIdAndVerificationCode(accountId, verificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_CODE));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setEmail(verification.getNewEmail());
        accountRepository.save(account);

        emailVerificationRepository.delete(verification);

        String subject = "Email Changed Successfully";
        String message = "Your email address has been successfully updated.";
        emailSenderService.sendSimpleEmail(account.getEmail(), subject, message);
    }

    public Page<AccountResponse> searchMember(
            String name,
            String bookTitle,
            String dateFrom,
            String dateTo,
            int page,
            int size) {

        // Tạo Pageable cho phân trang
        Pageable pageable = PageRequest.of(page, size);

        // Sử dụng Specification để tìm kiếm
        Specification<Account> spec = Specification.where(AccountSearchSpecification.hasName(name))
                .and(AccountSearchSpecification.hasBookTitle(bookTitle))
                .and(AccountSearchSpecification.isBornInDateRange(dateFrom, dateTo));

        // Lấy kết quả tìm kiếm với phân trang
        Page<Account> accounts = accountRepository.findAll(spec, pageable);

        // Map kết quả thành đối tượng Response
        return accounts.map(accountMapper::toAccountResponse);
    }
}
