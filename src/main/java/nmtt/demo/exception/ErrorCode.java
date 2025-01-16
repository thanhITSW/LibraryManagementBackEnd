package nmtt.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "User not existed", HttpStatus.NOT_FOUND),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Password must be at least 5 characters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Uncauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "You do not have permission", HttpStatus.FORBIDDEN)
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
