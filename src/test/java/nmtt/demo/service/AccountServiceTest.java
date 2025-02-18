package nmtt.demo.service;

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
import nmtt.demo.service.account.AccountService;
import nmtt.demo.service.email.EmailSenderService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @MockBean
    private EmailSenderService emailSenderService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private EmailVerificationRepository emailVerificationRepository;

    @MockBean
    private AccountMapper accountMapper;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private Authentication authentication;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private Account account1;
    private Account account2;

    private AccountCreationRequest request1;
    private AccountCreationRequest request2;
    private AccountResponse accountResponse1;
    private AccountResponse accountResponse2;
    private EmailVerification emailVerification;

    private LocalDate dob;

    private static final String EMAIL = "john@gmail.com";
    private static final String EMAIL_NOT_FOUND = "john99@gmail.com";
    private static final String NEW_EMAIL = "newjohn@example.com";
    private static final String VERIFICATION_CODE = "123456";
    private static final String ACCOUNT_ID = "1";
    private static final String ACCOUNT_ID_NOT_FOUND = "99";

    //data test search
    private String name = "John";
    private String bookTitle = "Book 1";
    private String dateFrom = "2020-01-01";
    private String dateTo = "2023-12-31";
    private int page = 0;
    private int size = 10;

    @BeforeEach
    void initData(){
        dob = LocalDate.of(1995, 1, 2);

        //account 1
        request1 = AccountCreationRequest.builder()
                .email(EMAIL)
                .firstName("John")
                .lastName("Doe")
                .password("oldPassword")
                .dob(dob)
                .build();

        accountResponse1 = AccountResponse.builder()
                .id("1")
                .email(EMAIL)
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        account1 = Account.builder()
                .id("1")
                .email(EMAIL)
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        //account 2
        request2 = AccountCreationRequest.builder()
                .email("john2@gmail.com")
                .firstName("John2")
                .lastName("Doe2")
                .password("oldPassword")
                .dob(dob)
                .build();

        accountResponse2 = AccountResponse.builder()
                .id("2")
                .email("john2@gmail.com")
                .firstName("John2")
                .lastName("Doe2")
                .dob(dob)
                .build();

        account2 = Account.builder()
                .id("2")
                .email("john2@gmail.com")
                .firstName("John2")
                .lastName("Doe2")
                .dob(dob)
                .build();

        emailVerification = new EmailVerification();
        emailVerification.setAccountId(ACCOUNT_ID);
        emailVerification.setNewEmail(NEW_EMAIL);
        emailVerification.setVerificationCode(VERIFICATION_CODE);
    }

    //createAccount
    @Test
    void createAccount_validRequest_success() {
        // GIVEN
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);

        // Mock accountMapper.toAccount(request1) to return valid object
        when(accountMapper.toAccount(any())).thenReturn(account1);

        when(accountRepository.save(any())).thenReturn(account1);
        when(accountMapper.toAccountResponse(any())).thenReturn(accountResponse1); // Mock response

        // WHEN
        var response = accountService.createAccount(request1);

        // THEN
        Assertions.assertThat(response.getId()).isEqualTo("1");
        Assertions.assertThat(response.getEmail()).isEqualTo("john@gmail.com");
    }

    @Test
    void createAccount_emailExisted_fail(){
        //GIVEN
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        //WHEN
        var exception = assertThrows(AppException.class,
                () -> accountService.createAccount(request1));

        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }

    //getAccount
    @Test
    void testGetAccount_WhenDataExists() {
        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

        when(accountMapper.toAccountResponse(account1)).thenReturn(accountResponse1);
        when(accountMapper.toAccountResponse(account2)).thenReturn(accountResponse2);

        List<AccountResponse> result = accountService.getAccount();

        assertEquals(2, result.size());
        assertEquals(accountResponse1, result.get(0));
        assertEquals(accountResponse2, result.get(1));

        // Check the number of method calls
        verify(accountRepository, times(1)).findAll();
        verify(accountMapper, times(1)).toAccountResponse(account1);
        verify(accountMapper, times(1)).toAccountResponse(account2);
    }

    @Test
    void testGetAccount_WhenNoData() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<AccountResponse> result = accountService.getAccount();

        assertEquals(0, result.size());

        verify(accountRepository, times(1)).findAll();
        verify(accountMapper, never()).toAccountResponse(any());
    }

    //getMyInfo
    @Test
    void testGetMyInfo_Success() {
        // Mock SecurityContext to simulate a logged in user
        when(securityContext
                .getAuthentication())
                .thenReturn(authentication);

        when(authentication
                .getName())
                .thenReturn(EMAIL);

        SecurityContextHolder.setContext(securityContext);

        when(accountRepository
                .findAccountByEmail(EMAIL))
                .thenReturn(Optional.of(account1));

        when(accountMapper
                .toAccountResponse(account1))
                .thenReturn(accountResponse1);

        AccountResponse result = accountService.getMyInfo();

        assertNotNull(result);
        assertEquals(accountResponse1, result);

        verify(accountRepository, times(1)).findAccountByEmail(EMAIL);
        verify(accountMapper, times(1)).toAccountResponse(account1);
    }

    @Test
    void testGetMyInfo_UserNotExisted() {
        when(securityContext
                .getAuthentication())
                .thenReturn(authentication);

        when(authentication
                .getName())
                .thenReturn(EMAIL);

        SecurityContextHolder.setContext(securityContext);

        when(accountRepository
                .findAccountByEmail(EMAIL))
                .thenReturn(Optional.empty());

        //check exception
        AppException exception = assertThrows(
                AppException.class,
                () -> accountService.getMyInfo());

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());

        verify(accountRepository, times(1)).findAccountByEmail(EMAIL);
        verify(accountMapper, never()).toAccountResponse(any());
    }

    //updateAccountById
    @Test
    void testUpdateAccountById_Success() {

        AccountUpdateRequest updateRequest = AccountUpdateRequest.builder()
                .email(EMAIL)
                .password("newPassword")
                .firstName("John")
                .lastName("Doe")
                .dob(account1.getDob())
                .roles(List.of("USER", "ADMIN"))
                .active(true)
                .build();

        // Mock Role
        Role role1 = new Role("USER", "User role", new HashSet<>());
        Role role2 = new Role("ADMIN", "Admin role", new HashSet<>());
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);

        // When find account
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(java.util.Optional.of(account1));

        // Mock Role Repository returns a list of permissions
        when(roleRepository.findAllById(updateRequest.getRoles())).thenReturn(roles);

        // Mock password encoder
        when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("encodedPassword");

        // Mock mapper
        doNothing().when(accountMapper).updateAccount(account1, updateRequest);
        when(accountMapper.toAccountResponse(account1)).thenReturn(accountResponse1);

        // When save account
        when(accountRepository.save(account1)).thenReturn(account1);

        // Call method service
        AccountResponse result = accountService.updateAccountById(ACCOUNT_ID, updateRequest);

        // Check result
        assertNotNull(result);
        assertEquals("encodedPassword", account1.getPassword());
        assertTrue(account1.getRoles().contains(role1));
        assertTrue(account1.getRoles().contains(role2));

        verify(accountRepository, times(1)).findById(ACCOUNT_ID);
        verify(roleRepository, times(1)).findAllById(updateRequest.getRoles());
        verify(passwordEncoder, times(1)).encode(updateRequest.getPassword());
        verify(accountMapper, times(1)).updateAccount(account1, updateRequest);
        verify(accountRepository, times(1)).save(account1);
    }

    @Test
    void testUpdateAccountById_AccountNotFound() {
        AccountUpdateRequest updateRequest = AccountUpdateRequest.builder()
                .email(EMAIL_NOT_FOUND)
                .password("newPassword")
                .firstName("John99")
                .lastName("Doe99")
                .dob(account1.getDob())
                .roles(List.of("USER"))
                .active(true)
                .build();

        when(accountRepository.findById(ACCOUNT_ID_NOT_FOUND)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> accountService
                        .updateAccountById(ACCOUNT_ID_NOT_FOUND, updateRequest));

        assertEquals("Account not found", exception.getMessage());

        verify(accountRepository, times(1)).findById(ACCOUNT_ID_NOT_FOUND);
        verify(accountRepository, never()).save(any());
    }

    //deleteUserById
    @Test
    void testDeleteUserById_Success() {
        // We use doNothing() because deleteById() does not return anything and is void
        doNothing().when(accountRepository).deleteById(ACCOUNT_ID);

        accountService.deleteUserById(ACCOUNT_ID);

        verify(accountRepository, times(1)).deleteById(ACCOUNT_ID);
    }

    @Test
    void testDeleteUserById_AccountNotFound() {

        // If the account is not found, deleteById should throw an exception
        doThrow(new RuntimeException("Account not found"))
                .when(accountRepository)
                .deleteById(ACCOUNT_ID_NOT_FOUND);

        assertThrows(RuntimeException.class
                , () -> accountService.deleteUserById(ACCOUNT_ID_NOT_FOUND));

        verify(accountRepository, times(1))
                .deleteById(ACCOUNT_ID_NOT_FOUND);
    }

    //resetPass
    @Test
    void testResetPass_AccountExists() {
        when(accountRepository
                .findAccountByEmail(EMAIL))
                .thenReturn(Optional.of(account1));
        when(passwordEncoder
                .encode(anyString()))
                .thenReturn("encodedNewPassword");

        accountService.resetPass(EMAIL);

        // Assert: Check if the account's password was updated
        verify(accountRepository, times(1)).save(account1);
        assertNotEquals("oldPassword", account1.getPassword());
        assertTrue(account1.getPassword().startsWith("encodedNewPassword"));

        // Assert: Check if email was sent
        verify(emailSenderService, times(1)).sendSimpleEmail(
                eq(EMAIL),
                eq("Reset Password"),
                contains("Your password has been reset.")
        );
    }

    @Test
    void testResetPass_AccountDoesNotExist() {
        when(accountRepository
                .findAccountByEmail(EMAIL_NOT_FOUND))
                .thenReturn(Optional.empty());

        // Act & Assert: Verify that AppException is thrown when account is not found
        assertThrows(AppException.class,
                () -> accountService.resetPass(EMAIL_NOT_FOUND));
    }

    //requestChangeMail
    @Test
    void testRequestChangeMail_AccountExists() {

        when(accountRepository
                .findById(ACCOUNT_ID))
                .thenReturn(Optional.of(account1));

        accountService.requestChangeMail(ACCOUNT_ID, NEW_EMAIL);

        // Assert: Verify that an EmailVerification entity was created and saved
        verify(emailVerificationRepository, times(1)).save(any(EmailVerification.class));

        // Assert: Verify that email was sent to the new email address
        verify(emailSenderService, times(1)).sendSimpleEmail(
                eq(NEW_EMAIL),
                eq("Verify New Email Address"),
                contains("Please use the following code to verify your new email address")
        );
    }

    @Test
    void testRequestChangeMail_AccountDoesNotExist() {

        when(accountRepository
                .findById(ACCOUNT_ID_NOT_FOUND))
                .thenReturn(Optional.empty());

        // Act & Assert: Verify that AppException is thrown when account is not found
        assertThrows(AppException.class
                , () -> accountService
                        .requestChangeMail(ACCOUNT_ID_NOT_FOUND, NEW_EMAIL));
    }

    //verifyChangeMail
    @Test
    void testVerifyChangeMail_Success() {
        when(accountRepository
                .findById(ACCOUNT_ID))
                .thenReturn(Optional.of(account1));
        when(emailVerificationRepository
                .findByAccountIdAndVerificationCode(ACCOUNT_ID, VERIFICATION_CODE))
                .thenReturn(Optional.of(emailVerification));

        accountService.verifyChangeMail(ACCOUNT_ID, VERIFICATION_CODE);

        assertEquals(NEW_EMAIL, account1.getEmail());

        // Assert: Verify that the EmailVerification entity was deleted
        verify(emailVerificationRepository, times(1)).delete(emailVerification);

        // Assert: Verify that the email notification was sent
        verify(emailSenderService, times(1)).sendSimpleEmail(
                eq(NEW_EMAIL),
                eq("Email Changed Successfully"),
                eq("Your email address has been successfully updated.")
        );
    }

    @Test
    void testVerifyChangeMail_InvalidVerificationCode() {
        when(emailVerificationRepository
                .findByAccountIdAndVerificationCode(ACCOUNT_ID, VERIFICATION_CODE))
                .thenReturn(Optional.empty());

        // Act & Assert: Verify that an AppException is thrown for an invalid verification code
        assertThrows(AppException.class
                , () -> accountService.verifyChangeMail(ACCOUNT_ID, VERIFICATION_CODE));
    }

    @Test
    void testVerifyChangeMail_AccountNotFound() {
        when(accountRepository
                .findById(ACCOUNT_ID_NOT_FOUND))
                .thenReturn(Optional.empty());

        // Act & Assert: Verify that an AppException is thrown for a non-existing account
        assertThrows(AppException.class,
                () -> accountService
                        .verifyChangeMail(ACCOUNT_ID_NOT_FOUND, VERIFICATION_CODE));
    }

    @Test
    void testSearchMember_WithValidParams() {

        // Arrange
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage = new PageImpl<>(List.of(account1), pageable, 1);
        when(accountRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(accountPage);
        when(accountMapper.toAccountResponse(account1)).thenReturn(accountResponse1);

        Page<AccountResponse> result = accountService.searchMember(name, bookTitle, dateFrom, dateTo, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(accountResponse1, result.getContent().get(0));

        // Verify interaction with the repository
        verify(accountRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMember_NoResultsFound() {
        // Arrange
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(accountRepository
                .findAll(any(Specification.class), eq(pageable)))
                .thenReturn(accountPage);

        Page<AccountResponse> result = accountService
                .searchMember(name, bookTitle, dateFrom, dateTo, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        // Verify interaction with the repository
        verify(accountRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMember_WithNullName() {
        // Arrange
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage =
                new PageImpl<>(List.of(account1), pageable, 1);

        when(accountRepository
                .findAll(any(Specification.class), eq(pageable)))
                .thenReturn(accountPage);

        when(accountMapper.toAccountResponse(account1)).thenReturn(accountResponse1);

        Page<AccountResponse> result = accountService.searchMember(null, bookTitle, dateFrom, dateTo, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interaction with the repository
        verify(accountRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMember_WithEmptyBookTitle() {
        // Arrange
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage =
                new PageImpl<>(List.of(account1), pageable, 1);

        when(accountRepository
                .findAll(any(Specification.class), eq(pageable)))
                .thenReturn(accountPage);

        when(accountMapper
                .toAccountResponse(account1))
                .thenReturn(accountResponse1);

        Page<AccountResponse> result = accountService
                .searchMember(name, "", dateFrom, dateTo, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interaction with the repository
        verify(accountRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMember_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPage =
                new PageImpl<>(List.of(account1), pageable, 10);

        when(accountRepository
                .findAll(any(Specification.class), eq(pageable)))
                .thenReturn(accountPage);

        when(accountMapper
                .toAccountResponse(account1))
                .thenReturn(accountResponse1);

        Page<AccountResponse> result = accountService
                .searchMember(name, bookTitle, dateFrom, dateTo, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertTrue(result.hasContent());

        // Verify interaction with the repository
        verify(accountRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }
}
