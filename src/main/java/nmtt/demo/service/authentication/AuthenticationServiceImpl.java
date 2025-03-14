package nmtt.demo.service.authentication;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AuthenticationRequest;
import nmtt.demo.dto.request.Account.LogoutRequest;
import nmtt.demo.dto.request.Account.RefreshRequest;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.InvalidatedToken;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.InvalidatedTokenRepository;
import nmtt.demo.service.system.SystemConfigService;
import nmtt.demo.utils.TokenUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AccountRepository accountRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final SystemConfigService systemConfigService;
    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;

    /**
     * Authenticates a user based on the provided email and password.
     *
     * @param request The authentication request containing the user's email and password.
     * @return An AuthenticationResponse containing the authentication token if successful, or a message indicating the account is not activated.
     * @throws AppException If the user does not exist or if the password does not match.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        var account = accountRepository
                .findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (systemConfigService.getConfig().isMaintenanceMode() &&
                account.getRoles().stream().anyMatch(role -> "USER".equalsIgnoreCase(role.getName()))) {
            throw new AppException(ErrorCode.FIX_SYSTEM);
        }

        if (!account.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        return !account.isFirstLogin() ?
                AuthenticationResponse.builder()
                        .access_token(tokenUtils.generateToken(account))
                        .refresh_token(tokenUtils.generateRefreshToken(account))
                        .build() :
                AuthenticationResponse.builder()
                        .firstLogin(account.isFirstLogin())
                        .access_token(tokenUtils.generateToken(account))
                        .refresh_token(tokenUtils.generateRefreshToken(account))
                        .build();
    }

    /**
     * Logs out a user by invalidating the provided token.
     *
     * @param request The logout request containing the token to be invalidated.
     * @throws ParseException If there is an error while parsing the token.
     * @throws JOSEException  If there is an error during the token verification process.
     */
    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = tokenUtils.verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder()
                            .id(jit)
                            .expiryTime(expiryTime)
                            .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    /**
     * Refreshes the JWT token for a user by verifying the provided token, invalidating it, and generating a new one.
     *
     * @param request The refresh request containing the token to be refreshed.
     * @return An AuthenticationResponse containing the new token and authentication status.
     * @throws ParseException If there is an error while parsing the token.
     * @throws JOSEException  If there is an error during the token verification process.
     * @throws AppException   If the token is invalid or the user is not found.
     */
    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = tokenUtils.verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var token_type = signedJWT.getJWTClaimsSet().getClaim("token_type");

        if (!token_type.equals("refresh")) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder()
                        .id(jit)
                        .expiryTime(expiryTime)
                        .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var email = signedJWT.getJWTClaimsSet().getSubject();

        var user = accountRepository
                .findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var access_token = tokenUtils.generateToken(user);
        var refresh_token = tokenUtils.regenerateRefreshToken(request.getToken());

        return AuthenticationResponse.builder()
                .access_token(access_token)
                .refresh_token(refresh_token)
                .build();
    }

    /**
     * Activates the user account associated with the provided token.
     *
     * @param token The JWT token containing the user's account ID.
     * @throws ParseException If there is an error while parsing the token.
     * @throws JOSEException  If there is an error during the token verification process.
     * @throws AppException   If the user account does not exist.
     */
    @Override
    public boolean activeAccount(String token) throws ParseException, JOSEException {

        var signedJWT = tokenUtils.verifyToken(token, true);

        String accountId = signedJWT.getJWTClaimsSet().getIssuer();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder()
                        .id(jit)
                        .expiryTime(expiryTime)
                        .build();

        invalidatedTokenRepository.save(invalidatedToken);

        account.setActive(true);

        accountRepository.save(account);
        return true;
    }
}