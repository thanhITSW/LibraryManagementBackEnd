package nmtt.demo.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmtt.demo.entity.Account;
import nmtt.demo.enums.ErrorCode;
import nmtt.demo.exception.AppException;
import nmtt.demo.repository.InvalidatedTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenUtils {
    @Value("${SIGNER_KEY}")
    private String SIGNER_KEY;

    @Value("${refreshable-duration}")
    private long REFRESHABLE_DURATION;

    @Value("${valid-duration}")
    private long VALID_DURATION;

    private final InvalidatedTokenRepository invalidatedTokenRepository;


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
     * Generates a new JWT token for the specified account.
     *
     * @param account The account for which the token is to be generated.
     * @return The generated JWT token as a string.
     * @throws RuntimeException If there is an error while generating the token.
     */
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
    public String regenerateRefreshToken(String oldRefreshToken) {
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
     * Builds a scope string for the given account based on its roles and permissions.
     *
     * @param account The account for which the scope is to be built.
     * @return A string representing the account's roles and permissions in the scope.
     */
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
}
