package nmtt.demo.exception;

import lombok.Data;
import nmtt.demo.enums.ErrorCode;

@Data
public class AppException extends RuntimeException{
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private ErrorCode errorCode;
}
