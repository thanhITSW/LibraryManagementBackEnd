package nmtt.demo.controller.common;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.AuthenticationRequest;
import nmtt.demo.dto.request.Account.IntrospectRequest;
import nmtt.demo.dto.request.Account.LogoutRequest;
import nmtt.demo.dto.request.Account.RefreshRequest;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.dto.response.Account.IntrospectResponse;
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

    @GetMapping("/{accountId}")
    public ResponseEntity<String> activeAccount(@PathVariable("accountId") String accountId) {
        authenticationService.activeAccount(accountId);
        return ResponseEntity.ok("Active account successfully");
    }
}
