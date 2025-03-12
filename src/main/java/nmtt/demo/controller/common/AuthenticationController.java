package nmtt.demo.controller.common;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.*;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.dto.response.Account.IntrospectResponse;
import nmtt.demo.dto.response.Email.VerificationResponse;
import nmtt.demo.service.authentication.AuthenticationService;
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

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse authResponse = authenticationService.authenticate(request);
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        AuthenticationResponse result = authenticationService.refreshToken(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(request);
        return ResponseEntity.ok(result);

    }

    @GetMapping("/active")
    public ResponseEntity<?> activeAccount(@RequestParam String token) throws ParseException, JOSEException {
        boolean isValid = authenticationService.activeAccount(token);

        if (isValid) {
            return ResponseEntity.ok(new VerificationResponse(true, "Email đã được xác nhận thành công"));
        }
        return ResponseEntity.status(401).body(new VerificationResponse(false, "Token không hợp lệ hoặc đã hết hạn"));
    }
}