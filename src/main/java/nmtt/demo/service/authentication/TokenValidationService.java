package nmtt.demo.service.authentication;

import com.nimbusds.jose.JOSEException;
import nmtt.demo.dto.request.Account.IntrospectRequest;
import nmtt.demo.dto.response.Account.IntrospectResponse;

import java.text.ParseException;

public interface TokenValidationService {
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
}
