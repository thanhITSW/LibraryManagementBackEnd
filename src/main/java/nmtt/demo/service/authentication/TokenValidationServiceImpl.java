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
