package nmtt.demo.enums;

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
    ACTIVITY_LOG_NOT_EXISTED(1002, "Activity log not existed", HttpStatus.NOT_FOUND),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Password must be at least 5 characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1003, "Email invalid", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1003, "Phone invalid", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Uncauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "You do not have permission", HttpStatus.FORBIDDEN),
    BOOK_EXISTED(1002, "Book existed", HttpStatus.BAD_REQUEST),
    INVALID_CSV_FORMAT(1006, "INVALID_CSV_FORMAT", HttpStatus.BAD_REQUEST),
    CSV_IMPORT_FAILED(1007, "CSV_IMPORT_FAILED", HttpStatus.BAD_REQUEST),
    INVALID_CSV_DATA(1007, "INVALID_CSV_DATA", HttpStatus.BAD_REQUEST),
    EMPTY_CSV_FILE(1007, "EMPTY_CSV_FILE", HttpStatus.BAD_REQUEST),
    INVALID_VERIFICATION_CODE(1007, "Invalid verification code", HttpStatus.BAD_REQUEST),
    BOOK_NOT_EXISTED(1008, "BOOK_NOT_EXISTED", HttpStatus.BAD_REQUEST),
    BORROWED_BOOK(1009, "You have already borrowed this book!", HttpStatus.BAD_REQUEST),
    NOT_AVAILABLE_BOOK(1009, "No copies of the book are available!", HttpStatus.BAD_REQUEST),
    BORROW_RECORD_NOT_FOUND(1009, "No borrowing record found for this user and book!", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1010, "OTP INVALID", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1011, "INVALID TOKEN", HttpStatus.UNAUTHORIZED),
    FIX_SYSTEM(1012, "System is under maintenance. Please try again later.", HttpStatus.BAD_GATEWAY),
    INVALID_JSON(1012, "INVALID_JSON", HttpStatus.BAD_REQUEST),
    NOT_FOUND(1012, "NOT_FOUND", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1012, "Password not match", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_MEDIA_TYPE(1013, "UNSUPPORTED_MEDIA_TYPE", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ACTIVE(1014, "Your account has not been activated, please activate your account", HttpStatus.BAD_REQUEST),
    NOT_DELETE_BOOK_WITH_ACTIVE(1014, "Cannot delete book with active borrowings", HttpStatus.BAD_REQUEST),
    NOT_DELETE_USER_WITH_ACTIVE(1014, "Cannot delete user with active borrowings", HttpStatus.BAD_REQUEST),
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
