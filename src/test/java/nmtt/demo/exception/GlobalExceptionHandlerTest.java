package nmtt.demo.exception;
import nmtt.demo.dto.request.Account.ApiResponse;
import nmtt.demo.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandlingRuntimeException() {
        RuntimeException exception = new RuntimeException("Unexpected Error");
        ResponseEntity<ApiResponse> response = exceptionHandler.handlingRuntimeException(exception);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(ErrorCode.UNCATEGORIZED_EXEPTION.getCode(), response.getBody().getCode());
    }

    @Test
    void testAppException() {
        AppException exception = new AppException(ErrorCode.EMAIL_INVALID);
        ResponseEntity<ApiResponse> response = exceptionHandler.AppException(exception);

        assertEquals(ErrorCode.EMAIL_INVALID.getStatusCode(), response.getStatusCode());
        assertEquals(ErrorCode.EMAIL_INVALID.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testHandlingAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("Access Denied");
        ResponseEntity<ApiResponse> response = exceptionHandler.handlingAccessDeniedException(exception);

        assertEquals(ErrorCode.UNAUTHORIZED.getStatusCode(), response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testHandleMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<String> response = exceptionHandler.handleMethodNotSupportedException(exception);

        assertEquals(405, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("HTTP method POST is not supported"));
    }
}
