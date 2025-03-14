package nmtt.demo.service.authentication;

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import nmtt.demo.dto.request.Account.IntrospectRequest;
import nmtt.demo.dto.response.Account.IntrospectResponse;
import nmtt.demo.exception.AppException;
import nmtt.demo.utils.TokenUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class TokenValidationServiceImpl implements TokenValidationService{
    private final TokenUtils tokenUtils;

    /**
     * Validates a JWT token and returns the introspection response.
     *
     * @param request The introspection request containing the token to be validated.
     * @return The introspection response indicating whether the token is valid or not.
     * @throws JOSEException If an error occurs during JWT verification.
     * @throws ParseException If an error occurs during parsing the token.
     */
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean invalid = true;
        try {
            tokenUtils.verifyToken(token, false);
        } catch (AppException e) {
            invalid = false;
        }

        return IntrospectResponse.builder()
                .valid(invalid)
                .build();
    }
}
