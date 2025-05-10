package nmtt.demo.controller.common;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.*;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.dto.response.Account.IntrospectResponse;
import nmtt.demo.dto.response.Email.VerificationResponse;
import nmtt.demo.service.authentication.AuthenticationService;
import nmtt.demo.service.authentication.TokenValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("${common-mapping}/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final TokenValidationService tokenValidationService;

    /**
     * Authenticates a user using their credentials.
     *
     * @param request The request containing the user's credentials.
     * @return A ResponseEntity containing the authentication response.
     *         If successful, the response contains the access token and refresh token.
     *         If unsuccessful, the response status is set to UNAUTHORIZED.
     * @throws AuthenticationException If the authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        try {
            AuthenticationResponse authResponse = authenticationService.authenticate(request);
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refreshes the user's access token using their refresh token.
     *
     * @param request The request containing the refresh token.
     * @return A ResponseEntity containing the authentication response.
     *         If successful, the response contains the refreshed access token and refresh token.
     *         If unsuccessful, the response status is set to INTERNAL_SERVER_ERROR.
     * @throws ParseException     If the refresh token is not in a valid format.
     * @throws JOSEException       If an error occurs during JWT processing.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        AuthenticationResponse result = authenticationService.refreshToken(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Logs out the user by invalidating their access and refresh tokens.
     *
     * @param request The request containing the user's access token and refresh token.
     * @return A ResponseEntity with a status of NO_CONTENT if the logout is successful.
     *         If an error occurs during JWT processing, a ParseException is thrown.
     *         If an error occurs during the logout process, a JOSEException is thrown.
     * @throws ParseException     If the access or refresh token is not in a valid format.
     * @throws JOSEException       If an error occurs during JWT processing.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Authenticates a user's access token and returns its introspection details.
     *
     * @param request The request containing the user's access token.
     * @return A ResponseEntity containing the introspection response.
     *         If successful, the response contains the introspection details of the access token.
     *         If unsuccessful, the response status is set to INTERNAL_SERVER_ERROR.
     * @throws ParseException     If the access token is not in a valid format.
     * @throws JOSEException       If an error occurs during JWT processing.
     */
    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        IntrospectResponse result = tokenValidationService.introspect(request);
        return ResponseEntity.ok(result);

    }

    /**
     * Activates a user's account by validating the provided token.
     *
     * @param token The token sent to the user's email for account activation.
     * @return A ResponseEntity containing the verification response.
     *         If successful, the response contains a boolean value indicating successful activation and a success message.
     *         If unsuccessful, the response contains a boolean value indicating failed activation and an error message.
     *         The response status is set to OK if successful, and to UNAUTHORIZED if unsuccessful.
     * @throws ParseException     If the token is not in a valid format.
     * @throws JOSEException       If an error occurs during JWT processing.
     */
    @GetMapping("/active")
    public ResponseEntity<?> activeAccount(@RequestParam String token) throws ParseException, JOSEException {
        boolean isValid = authenticationService.activeAccount(token);

        if (isValid) {
            return ResponseEntity.ok(new VerificationResponse(true, "Email đã được xác nhận thành công"));
        }
        return ResponseEntity.status(401).body(new VerificationResponse(false, "Token không hợp lệ hoặc đã hết hạn"));
    }
}