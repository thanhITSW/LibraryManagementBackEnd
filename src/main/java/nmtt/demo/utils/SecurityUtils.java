package nmtt.demo.utils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;

/**
 * This utility class provides methods for accessing security-related information.
 */
public class SecurityUtils {
    /**
     * Retrieves the issuer of the JWT token from the current security context.
     *
     * @return The issuer of the JWT token, or {@code null} if the JWT token is not present or not a valid instance of {@link Jwt}.
     */
    public static String getIssuer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return jwt.getClaim("iss");
        }
        return null;
    }
}
