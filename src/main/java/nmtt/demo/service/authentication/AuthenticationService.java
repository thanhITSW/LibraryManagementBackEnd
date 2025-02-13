package nmtt.demo.service.authentication;

import com.nimbusds.jose.*;
import nmtt.demo.dto.request.Account.AuthenticationRequest;
import nmtt.demo.dto.request.Account.IntrospectRequest;
import nmtt.demo.dto.request.Account.LogoutRequest;
import nmtt.demo.dto.request.Account.RefreshRequest;
import nmtt.demo.dto.response.Account.AuthenticationResponse;
import nmtt.demo.dto.response.Account.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {
    public AuthenticationResponse authenticate(AuthenticationRequest request);
    public void logout(LogoutRequest request) throws ParseException, JOSEException;
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    public void activeAccount(String accountId);
}
