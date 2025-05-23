package nmtt.demo.service.account;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nmtt.demo.constant.PredefinedRole;
import nmtt.demo.dto.request.Account.*;
import nmtt.demo.dto.response.Account.AccountResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.EmailVerification;
import nmtt.demo.entity.OtpPhone;
import nmtt.demo.entity.Role;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.mapper.AccountMapper;
import nmtt.demo.repository.*;
import nmtt.demo.service.activity_log.ActivityLogService;
import nmtt.demo.service.email.EmailSenderService;
import nmtt.demo.utils.SecurityUtils;
import nmtt.demo.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailSenderService emailSenderService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final OtpPhoneRepository otpPhoneRepository;
    private final ActivityLogService logService;
    private final TokenUtils tokenUtils;
    private final BorrowingRepository borrowingRepository;

    @Value("${URL_CLIENT}")
    private String url_client;

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
        var token = tokenUtils.generateToken(account);

        String url_active = url_client + "/verify-email?token=" + token;

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #007bff;'>Activate Your Account</h2>"
                + "<p>Hello,</p>"
                + "<p>Thank you for signing up. Please click the button below to activate your account:</p>"
                + "<a href='" + url_active + "' target='_blank' style='display: inline-block; background-color: #007bff; color: #ffffff; padding: 10px 20px; font-size: 16px; text-decoration: none; border-radius: 5px; margin-top: 10px;'>"
                + "Activate Now</a>"
                + "<p>If you did not register for this account, please ignore this email.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";

        emailSenderService.sendHtmlEmail(account.getEmail(), "Verify account"
                , htmlContent);

        return accountMapper.toAccountResponse(account);
    }

    /**
     * Admin creates a new user account.
     *
     * @param request The account creation request containing user details.
     * @return The created account as an AccountResponse.
     * @throws AppException If the email is already registered.
     */

    @Override
    public AccountResponse adminCreateAccount(AdminCreationAccountRequest request){
        if(accountRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Account account = accountMapper.adminToAccount(request);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            request.getRoles().forEach(roleName ->
                    roleRepository.findById(roleName).ifPresent(roles::add)
            );
        }

        if (roles.isEmpty()) {
            roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        }

        account.setRoles(roles);
        account = accountRepository.save(account);

        Map<String, Object> newData = toMap(account);
        logService.log("CREATE", "ACCOUNT", account.getId(),
                "Admin created a new account", null, newData);

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #007bff;'>Your New Account Has Been Created</h2>"
                + "<p>Hello,</p>"
                + "<p>We are pleased to inform you that your account has been successfully created.</p>"
                + "<p><strong>Account Details:</strong></p>"
                + "<p><b>Email:</b> " + request.getEmail() + "</p>"
                + "<p><b>Password:</b> " + request.getPassword() + "</p>"
                + "<p>For security reasons, we recommend changing your password after logging in.</p>"
                + "<p>If you have any questions, please contact our support team.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";

        emailSenderService.sendHtmlEmail(account.getEmail(), "Create a new account"
                , htmlContent);

        return accountMapper.toAccountResponse(account);
    }

    /**
     * Retrieves a list of all user accounts.
     *
     * @return A list of AccountResponse objects representing all accounts.
     */
    @Override
    public List<AccountResponse> getAccount(){
        return accountRepository.findAll()
                .stream()
                .filter(account -> account.getRoles().stream()
                        .anyMatch(role -> "USER".equals(role.getName())))
                .map(accountMapper::toAccountResponse)
                .toList();
    }

    /**
     * Retrieves the authenticated user's account information.
     *
     * @return The authenticated user's account details as an AccountResponse.
     * @throws AppException If the user does not exist.
     */
    @Override
    public AccountResponse getMyInfo(){
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;

        Account account = accountRepository
                .findById(issuer)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return accountMapper.toAccountResponse(account);
    }

    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id The unique identifier of the account to retrieve.
     * @return An AccountResponse object containing the account details.
     * @throws AppException If no account is found with the given id, with error code USER_NOT_EXISTED.
     */
    @Override
    public AccountResponse getAccountById(String id){
        Account account = accountRepository
               .findById(id)
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
                .orElseThrow(() -> new RuntimeException("Account not found") );

        Map<String, Object> oldData = toMap(account);

        accountMapper.updateAccount(account, request);

        var roles = roleRepository
                .findAllById(request.getRoles());

        account.setRoles(new HashSet<>(roles));

        Account updatedAccount = accountRepository.save(account);
        Map<String, Object> newData = toMap(updatedAccount);
        logService.log("UPDATE", "ACCOUNT", accountId,
                "Admin updated account", oldData, newData);
        return accountMapper.toAccountResponse(updatedAccount);
    }

    /**
     * Deletes a user account by its ID.
     *
     * @param accountId The ID of the account to be deleted.
     */
    @Transactional
    @Override
    public void deleteUserById(String accountId){
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found") );

        if (borrowingRepository.existsByAccountId(accountId)) {
            throw new AppException(ErrorCode.NOT_DELETE_USER_WITH_ACTIVE);
        }

        Map<String, Object> oldData = toMap(account);
        accountRepository.deleteById(accountId);

        logService.log("DELETE", "ACCOUNT", accountId,
                "Admin deleted account", oldData, null);
    }

    /**
     * Resets the user's password and sends the new password via email.
     *
     * @param request the request containing the user's email
     * @throws AppException if the user does not exist
     */
    @Override
    public void resetPass(EmailRequest request) {
        Account account = accountRepository
                .findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newPassword = String.format("%06d", (int) (Math.random() * 1000000));
        account.setPassword(passwordEncoder.encode(newPassword));
        account.setFirstLogin(true);

        accountRepository.save(account);

        String subject = "Reset Password";
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #007bff;'>Reset Your Password</h2>"
                + "<p>Hello,</p>"
                + "<p>You have requested to reset your password. Below is your new temporary password:</p>"
                + "<p style='font-size: 18px; font-weight: bold; color: #d9534f;'>" + newPassword + "</p>"
                + "<p>Please log in using this password and change it immediately for security purposes.</p>"
                + "<p>If you did not request this reset, please contact our support team immediately.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";

        emailSenderService.sendHtmlEmail(request.getEmail(), subject, htmlContent);
    }

    /**
     * Sends a verification code to the user's new email address for email change confirmation.
     *
     * @param request the request containing the new email address
     * @throws AppException if the user does not exist
     */
    @Override
    public void requestChangeMail(EmailRequest request) {
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
        Account account = accountRepository
                .findById(issuer)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(account.getEmail().equals(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }

        String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

        EmailVerification verification = new EmailVerification();
        verification.setAccountId(issuer);
        verification.setNewEmail(request.getEmail());
        verification.setVerificationCode(verificationCode);

        emailVerificationRepository.save(verification);

        // send email authentication
        String subject = "Verify New Email Address";
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #007bff;'>Verify Your Email Change Request</h2>"
                + "<p>Hello,</p>"
                + "<p>We received a request to change your email address. Please use the verification code below to confirm this change:</p>"
                + "<p style='font-size: 22px; font-weight: bold; color: #d9534f;'>" + verificationCode + "</p>"
                + "<p>This code is valid for a limited time. If you did not request this change, please ignore this email.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";

        emailSenderService.sendHtmlEmail(request.getEmail(), subject, htmlContent);
    }

    /**
     * Verifies the email change request using the provided verification code and updates the user's email address.
     *
     * @param request The verification code sent to the new email.
     * @throws AppException If the verification code is invalid or the user does not exist.
     */
    @Transactional
    @Override
    public void verifyChangeMail(VerifyCodeRequest request) {
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
        EmailVerification verification = emailVerificationRepository
                .findByAccountIdAndVerificationCode(issuer, request.getOtp())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_CODE));

        Account account = accountRepository
                .findById(issuer)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Map<String, Object> oldData = toMap(account);

        account.setEmail(verification.getNewEmail());
        Account updatedAccount = accountRepository.save(account);

        logService.log("UPDATE", "ACCOUNT", account.getId(),
                "User changed email", oldData, toMap(updatedAccount));

        emailVerificationRepository.delete(verification);

        String subject = "Email Changed Successfully";
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #28a745;'>Email Updated Successfully</h2>"
                + "<p>Hello,</p>"
                + "<p>Your email address has been successfully updated in our system.</p>"
                + "<p>If you did not request this change, please contact our support team immediately.</p>"
                + "<p>Thank you for using our services.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";
        emailSenderService.sendHtmlEmail(account.getEmail(), subject, htmlContent);
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param request contains old and new passwords.
     * @throws AppException if the user does not exist.
     * @throws RuntimeException if the old password is incorrect.
     */
    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request){
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;

        Account account = accountRepository
               .findById(issuer)
               .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(!passwordEncoder.matches(request.getOldPassword(), account.getPassword())){
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    /**
     * Initiates a request to change the user's phone number.
     * This method generates an OTP, saves it with the new phone number,
     * and sends a verification email to the user's current email address.
     *
     * @param request The ChangePhoneRequest containing the new phone number.
     * @return The generated OTP as a String.
     * @throws AppException If the user does not exist (USER_NOT_EXISTED) or if the new phone number is the same as the current one (PHONE_INVALID).
     */
    @Override
    public String requestChangePhone(ChangePhoneRequest request) {
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;
    
        Account account = accountRepository
                .findById(issuer)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    
        if(account.getPhone().equals(request.getPhone())){
            throw new AppException(ErrorCode.PHONE_INVALID);
        }
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
    
        LocalDateTime createdAt = LocalDateTime.now();
    
        OtpPhone otpPhone = OtpPhone.builder()
                .accountId(issuer)
                .phone(request.getPhone())
                .otp(otp)
                .createdAt(createdAt)
                .build();
    
        otpPhoneRepository.save(otpPhone);
    
        String subject = "Verify New Phone";
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #007bff;'>Verify Your Phone Change Request</h2>"
                + "<p>Hello,</p>"
                + "<p>We received a request to change your phone. Please use the verification code below to confirm this change:</p>"
                + "<p style='font-size: 22px; font-weight: bold; color: #d9534f;'>" + otp + "</p>"
                + "<p>This code is valid for a limited time. If you did not request this change, please ignore this email.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";
        emailSenderService.sendHtmlEmail(account.getEmail(), subject, htmlContent);
        return otp;
    }

    /**
     * Verifies the phone change request using the provided OTP and updates the user's phone number.
     * This method checks the validity of the OTP, updates the account with the new phone number,
     * logs the change, and removes the used OTP from the repository.
     *
     * @param request The VerifyCodeRequest containing the OTP sent to the user.
     * @throws AppException If the OTP is invalid (INVALID_OTP) or if the user does not exist (USER_NOT_EXISTED).
     */
    @Override
    @Transactional
    public void verifyChangePhone(VerifyCodeRequest request) {
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;

        OtpPhone otpPhone = otpPhoneRepository
               .findByOtpAndAccountIdAndCreatedAtAfter(request.getOtp(), issuer, LocalDateTime.now().minusMinutes(5))
               .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        Account account = accountRepository
               .findById(issuer)
               .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Map<String, Object> oldData = toMap(account);

        String newPhone = otpPhone.getPhone();
        Account updateAccount = accountRepository
               .findById(otpPhone.getAccountId())
               .map(ac -> {
                    ac.setPhone(newPhone);
                    return accountRepository.save(ac);
                })
               .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        logService.log("UPDATE", "ACCOUNT", account.getId(),
                "User changed phone", oldData, toMap(updateAccount));

        otpPhoneRepository.delete(otpPhone);
    }

    /**
     * Resends the account activation link to the user's email.
     * <p>
     * This method checks if the account associated with the provided email is inactive,
     * generates a new activation token, and sends an activation email to the user.
     * </p>
     *
     * @param request The EmailRequest object containing the user's email address.
     * @throws AppException If the user does not exist (USER_NOT_EXISTED) or if the account is already active (USER_EXISTED).
     */
    @Override
    public void resendLinkActiveAccount(EmailRequest request){
        Account account = accountRepository.findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(account.isActive()){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        var token = tokenUtils.generateToken(account);

        String url_active = url_client + "/verify-email?token=" + token;

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; text-align: center;'>"
                + "<h2 style='color: #007bff;'>Activate Your Account</h2>"
                + "<p>Hello,</p>"
                + "<p>Thank you for signing up. Please click the button below to activate your account:</p>"
                + "<a href='" + url_active + "' target='_blank' style='display: inline-block; background-color: #007bff; color: #ffffff; padding: 10px 20px; font-size: 16px; text-decoration: none; border-radius: 5px; margin-top: 10px;'>"
                + "Activate Now</a>"
                + "<p>If you did not register for this account, please ignore this email.</p>"
                + "<p>Best regards,<br>The SparkMinds Team</p>"
                + "</div>";

        emailSenderService.sendHtmlEmail(account.getEmail(), "Verify account"
                , htmlContent);
    }

    /**
     * Changes the password for a user during their first login.
     * <p>
     * This method updates the user's password with the new password provided in the request
     * and marks the account as no longer being in the first login state.
     * </p>
     *
     * @param request The FirstLoginRequest object containing the new password.
     * @throws AppException If the user does not exist (USER_NOT_EXISTED).
     */
    @Override
    public void changePasswordFirstLogin(FirstLoginRequest request){
        String issuer = SecurityUtils.getIssuer();
        assert issuer != null;

        Account account = accountRepository
                .findById(issuer)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setFirstLogin(false);
        accountRepository.save(account);
    }

    /**
     * Converts an Account object into a Map containing the account's details.
     *
     * @param account The Account object to be converted.
     * @return A Map containing the account's details.
     * The Map contains the following keys and their corresponding values:
     * - "id": The account's unique identifier.
     * - "email": The account's email address.
     * - "firstName": The account's first name.
     * - "lastName": The account's last name.
     * - "dob": The account's date of birth.
     * - "phone": The account's phone number.
     * - "active": A boolean indicating whether the account is active.
     */
    private Map<String, Object> toMap(Account account) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", account.getId());
        data.put("email", account.getEmail());
        data.put("firstName", account.getFirstName());
        data.put("lastName", account.getLastName());
        data.put("dob", account.getDob());
        data.put("phone", account.getPhone());
        data.put("active", account.isActive());
        return data;
    }
}
