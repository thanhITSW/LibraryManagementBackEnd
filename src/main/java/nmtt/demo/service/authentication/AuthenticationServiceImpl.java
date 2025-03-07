package nmtt.demo.service.authentication;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.dto.request.Account.AuthenticationRequest;
import nmtt.demo.dto.request.Account.IntrospectRequest;
import nmtt.demo.dto.request.Account.LogoutRequest;
import nmtt.demo.dto.request.Account.RefreshRequest;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.dto.response.Account.IntrospectResponse;
import nmtt.demo.entity.Account;
import nmtt.demo.entity.InvalidatedToken;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.AccountRepository;
import nmtt.demo.repository.InvalidatedTokenRepository;
import nmtt.demo.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AccountRepository accountRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final SystemConfigService systemConfigService;

    @Value("${SIGNER_KEY}")
    private String SIGNER_KEY;

    @Value("${refreshable-duration}")
    private long REFRESHABLE_DURATION;

    @Value("${valid-duration}")
    private long VALID_DURATION;

    /**
     * Authenticates a user based on the provided email and password.
     *
     * @param request The authentication request containing the user's email and password.
     * @return An AuthenticationResponse containing the authentication token if successful, or a message indicating the account is not activated.
     * @throws AppException If the user does not exist or if the password does not match.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var account = accountRepository
                .findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder
                .matches(request.getPassword(), account.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        if (systemConfigService.getConfig().isMaintenanceMode() &&
                account.getRoles().stream().anyMatch(role -> "USER".equalsIgnoreCase(role.getName()))) {
            throw new AppException(ErrorCode.FIX_SYSTEM);
        }

        if (!account.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        return AuthenticationResponse.builder()
                .access_token(generateToken(account))
                .refresh_token(generateRefreshToken(account))
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
            var signToken = verifyToken(request.getToken(), true);

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
     * Verifies a JWT token, ensuring it is valid and not expired.
     * Optionally checks if the token is a refresh token with a customizable expiration duration.
     *
     * @param token     The JWT token to be verified.
     * @param isRefresh A flag indicating if the token is a refresh token.
     * @return The verified SignedJWT if valid.
     * @throws JOSEException  If there is an error during the verification process.
     * @throws ParseException If there is an error while parsing the token.
     * @throws AppException   If the token is invalid, expired, or has been invalidated.
     */
    @Override
    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
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
        var signedJWT = verifyToken(request.getToken(), true);

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

        var access_token = generateToken(user);
        var refresh_token = regenerateRefreshToken(request.getToken());

        return AuthenticationResponse.builder()
                .access_token(access_token)
                .refresh_token(refresh_token)
                .build();
    }

    /**
     * Generates a new JWT token for the specified account.
     *
     * @param account The account for which the token is to be generated.
     * @return The generated JWT token as a string.
     * @throws RuntimeException If there is an error while generating the token.
     */
    @Override
    public String generateToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
                .issuer(account.getId())
                .issueTime(new Date())
                .claim("token_type", "access")
                .expirationTime(new Date(
                        Instant.now()
                                .plus(VALID_DURATION, ChronoUnit.SECONDS)
                                .toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a signed refresh token (JWT) for the given account.
     * The token includes claims like subject (email), issuer (account ID),
     * issue time, expiration, scope, and a unique ID.
     *
     * @param account The account to generate the refresh token for.
     * @return A signed JWT refresh token.
     * @throws RuntimeException If token creation fails.
     */
    @Override
    public String generateRefreshToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
                .issuer(account.getId())
                .issueTime(new Date())
                .claim("token_type", "refresh")
                .expirationTime(new Date(
                        Instant.now()
                                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                                .toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Regenerates a refresh token based on an existing old refresh token.
     * This method creates a new JWT with the same claims as the old token,
     * but with a new issue time and JWT ID. The expiration time is preserved
     * from the old token.
     *
     * @param oldRefreshToken The existing refresh token to be regenerated.
     * @return A new JWT refresh token as a serialized string.
     * @throws RuntimeException If there's an error parsing the old token or
     *                          signing the new token.
     */
    private String regenerateRefreshToken(String oldRefreshToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(oldRefreshToken);
            JWTClaimsSet oldClaims = signedJWT.getJWTClaimsSet();

            Date expirationTime = oldClaims.getExpirationTime();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWTClaimsSet newClaims = new JWTClaimsSet.Builder()
                    .subject(oldClaims.getSubject())
                    .issuer(oldClaims.getIssuer())
                    .issueTime(new Date())
                    .expirationTime(expirationTime)
                    .jwtID(UUID.randomUUID().toString())
                    .claim("token_type", "refresh")
                    .claim("scope", oldClaims.getClaim("scope"))
                    .build();

            SignedJWT newToken = new SignedJWT(header, newClaims);
            newToken.sign(new MACSigner(SIGNER_KEY.getBytes()));

            return newToken.serialize();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Error regenerating refresh token", e);
        }
    }

    /**
     * Introspects a given token to check if it is valid.
     *
     * @param request The request containing the token to be introspected.
     * @return An IntrospectResponse indicating whether the token is valid.
     * @throws JOSEException  If there is an error during the token verification process.
     * @throws ParseException If there is an error while parsing the token.
     */
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean invalid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            invalid = false;
        }

        return IntrospectResponse.builder()
                .valid(invalid)
                .build();
    }

    /**
     * Builds a scope string for the given account based on its roles and permissions.
     *
     * @param account The account for which the scope is to be built.
     * @return A string representing the account's roles and permissions in the scope.
     */
    @Override
    public String buildScope(Account account) {
        StringJoiner stringJoiner = new StringJoiner("");
        if (!CollectionUtils.isEmpty(account.getRoles())) {
            account.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());

                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }

        return stringJoiner.toString();
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

        var signedJWT = verifyToken(token, true);

        String accountId = signedJWT.getJWTClaimsSet().getIssuer();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setActive(true);

        accountRepository.save(account);
        return true;
    }
}