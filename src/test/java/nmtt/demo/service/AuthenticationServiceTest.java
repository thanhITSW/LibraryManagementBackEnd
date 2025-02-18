package nmtt.demo.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import nmtt.demo.components.CustomJwtDecoder;
import nmtt.demo.dto.request.Account.AuthenticationRequest;
import nmtt.demo.dto.request.Account.IntrospectRequest;
import nmtt.demo.dto.request.Account.LogoutRequest;
import nmtt.demo.dto.request.Account.RefreshRequest;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.dto.response.Account.IntrospectResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.InvalidatedToken;
import nmtt.demo.entity.Permission;
import nmtt.demo.entity.Role;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.InvalidatedTokenRepository;
import nmtt.demo.service.authentication.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/test.properties")
public class AuthenticationServiceTest {
    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthenticationRequest validRequest;
    private AuthenticationRequest invalidRequest;
    private Account validAccount;
    private RefreshRequest refreshRequest;
    private String validToken;

    @Value("${SIGNER_KEY}")
    private String SIGNER_KEY;

    @Value("${refreshable-duration}")
    private long REFRESHABLE_DURATION;

    @Value("${valid-duration}")
    private long VALID_DURATION;

    @BeforeEach
    public void setUp() throws ParseException, JOSEException {
        MockitoAnnotations.openMocks(this);

        validRequest = new AuthenticationRequest("john@gmail.com", "validPassword");
        invalidRequest = new AuthenticationRequest("john@gmail.com", "wrongPassword");

        // Set up Account entity with active status and a hashed password
        validAccount = Account.builder()
                .id("1")
                .email("john@gmail.com")
                .password(passwordEncoder.encode("validPassword"))
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .active(true)
                .build();

        validToken = authenticationService.generateToken(validAccount);
        refreshRequest = RefreshRequest.builder().token(validToken).build();

        when(accountRepository.findAccountByEmail(validAccount.getEmail())).thenReturn(Optional.of(validAccount));

    }

    @Test
    public void testAuthenticate_SuccessfulLogin() {
        when(accountRepository.findAccountByEmail(validRequest.getEmail())).thenReturn(Optional.of(validAccount));

        // Test successful authentication
        AuthenticationResponse response = authenticationService.authenticate(validRequest);

        assertNotNull(response);
        assertTrue(response.isAuthenticated());
        assertNotNull(response.getToken());
    }

    @Test
    public void testAuthenticate_UserNotFound() {
        when(accountRepository.findAccountByEmail(invalidRequest.getEmail())).thenReturn(Optional.empty());

        // Test that the exception is thrown
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.authenticate(invalidRequest);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    public void testAuthenticate_FailedLogin() {
        when(accountRepository.findAccountByEmail(invalidRequest.getEmail())).thenReturn(Optional.of(validAccount));

        // Test failed authentication
        AppException exception = assertThrows(AppException.class, () -> {
            authenticationService.authenticate(invalidRequest);
        });

        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    @Test
    public void testAuthenticate_AccountInactive() {
        // Set account to inactive
        validAccount.setActive(false);

        when(accountRepository.findAccountByEmail(validRequest.getEmail())).thenReturn(Optional.of(validAccount));

        // Test inactive account response
        AuthenticationResponse response = authenticationService.authenticate(validRequest);

        assertNotNull(response);
        assertFalse(response.isAuthenticated());
        assertEquals("Tài khoản của bạn chưa được kích hoạt", response.getToken());
    }

    @Test
    public void testLogout() throws ParseException, JOSEException {
        // Arrange
        String token = authenticationService.generateToken(validAccount); // Assume this method generates a valid JWT token.

        // Prepare the mock responses
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);
        when(invalidatedTokenRepository.save(any(InvalidatedToken.class))).thenReturn(new InvalidatedToken());

        // Act
        authenticationService.logout(new LogoutRequest(token));

        // Assert
        verify(invalidatedTokenRepository, times(1)).save(any(InvalidatedToken.class));
    }

    @Test
    public void testRefreshToken_withValidToken_returnsNewToken() throws JOSEException, ParseException {
        // Mocking the dependencies
        when(accountRepository
                .findAccountByEmail(validAccount.getEmail()))
                .thenReturn(Optional.of(validAccount));
        when(invalidatedTokenRepository
                .existsById(anyString()))
                .thenReturn(false); // Token is not invalidated

        // Test refreshing the token
        AuthenticationResponse response = authenticationService
                .refreshToken(refreshRequest);

        assertTrue(response.isAuthenticated());
        assertNotNull(response.getToken());
    }

    @Test
    public void testRefreshToken_withInvalidToken_throwsAppException() throws JOSEException, ParseException {
        // Mocking the invalid token scenario
        when(accountRepository
                .findAccountByEmail(validAccount
                        .getEmail()))
                .thenReturn(Optional.empty());

        // Test with an invalid token
        assertThrows(AppException.class,
                () -> authenticationService.refreshToken(refreshRequest));
    }

    @Test
    public void testRefreshToken_withInvalidatedToken_throwsAppException() throws JOSEException, ParseException {
        // Simulating that the token is invalidated
        when(accountRepository.findAccountByEmail(validAccount.getEmail())).thenReturn(Optional.of(validAccount));
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(true);

        assertThrows(AppException.class, () -> authenticationService.refreshToken(refreshRequest));
    }

    @Test
    public void testActiveAccount_withValidAccountId() {
        when(accountRepository.findById(validAccount.getId())).thenReturn(Optional.of(validAccount));

        authenticationService.activeAccount(validAccount.getId());

        assertTrue(validAccount.isActive(), "Account should be activated");

        verify(accountRepository, times(1)).save(validAccount);
    }

    @Test
    public void testActiveAccount_withNonExistentAccount() {
        when(accountRepository.findById(validAccount.getId())).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> authenticationService.activeAccount(validAccount.getId()));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testBuildScope_withRolesAndPermissions() {
        // Set up mock data for permissions
        Permission permission1 = new Permission("READ", "Read permission");
        Permission permission2 = new Permission("WRITE", "Write permission");

        // Set up mock data for roles
        Role role1 = new Role("ADMIN", "Administrator role", Set.of(permission1, permission2));
        Role role2 = new Role("USER", "User role", Set.of(permission1));

        // Set up mock account
        Account account = new Account();
        account.setRoles(Set.of(role1, role2));

        // Call buildScope
        String scope = authenticationService.buildScope(account);
        System.out.println(scope);

        // Assert that the scope string is built correctly
        assertTrue(scope.contains("ROLE_USERREAD"));
    }

    @Test
    public void testIntrospect_validToken() throws JOSEException, ParseException {
        String validToken = authenticationService.generateToken(validAccount); // A utility method to generate a valid token

        boolean isValid = authenticationService.introspect(new IntrospectRequest(validToken)).isValid();

        // Assert that the token is valid
        assertTrue(isValid);
    }
}